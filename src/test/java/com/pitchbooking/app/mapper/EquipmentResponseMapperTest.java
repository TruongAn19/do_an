package com.pitchbooking.app.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pitchbooking.app.domain.Equipment;
import com.pitchbooking.app.domain.Product;
import com.pitchbooking.app.domain.Role;
import com.pitchbooking.app.domain.User;
import com.pitchbooking.app.domain.dto.EquipmentResponseDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EquipmentResponseMapperTest {

    private final EquipmentResponseMapper mapper = new EquipmentResponseMapper();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void equipmentDtoKeepsFrontendContractWithoutEntityGraph() throws Exception {
        Equipment equipment = equipmentWithCyclicOwnerGraph();

        EquipmentResponseDTO dto = mapper.toDTO(equipment);
        String json = objectMapper.writeValueAsString(dto);
        JsonNode root = objectMapper.readTree(json);

        assertThat(root.path("id").asLong()).isEqualTo(10L);
        assertThat(root.path("product").path("id").asLong()).isEqualTo(20L);
        assertThat(root.path("product").path("name").asText()).isEqualTo("Cụm sân Quận 1");
        assertThat(json).doesNotContain("password", "users", "role", "products");
        assertThat(json.length()).isLessThan(1_000);
    }

    @Test
    void entitySafetyAnnotationsPreventPasswordLeakAndRoleRecursion() throws Exception {
        Equipment equipment = equipmentWithCyclicOwnerGraph();

        String json = objectMapper.writeValueAsString(equipment);

        assertThat(json).doesNotContain("secret-hash", "\"password\"", "\"users\"");
        assertThat(json.length()).isLessThan(2_000);
    }

    private Equipment equipmentWithCyclicOwnerGraph() {
        Role role = new Role();
        role.setId(1L);
        role.setName("ADMIN");

        User owner = new User();
        owner.setId(1L);
        owner.setEmail("admin@example.com");
        owner.setPassword("secret-hash");
        owner.setRole(role);
        role.setUsers(List.of(owner));

        Product product = new Product();
        product.setId(20L);
        product.setName("Cụm sân Quận 1");
        product.setUser(owner);
        owner.setProducts(List.of(product));

        Equipment equipment = new Equipment();
        equipment.setId(10L);
        equipment.setName("Bóng đá");
        equipment.setQuantity(5);
        equipment.setProduct(product);
        return equipment;
    }
}
