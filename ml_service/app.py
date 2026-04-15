from flask import Flask, jsonify, request
import pandas as pd
# Lưu ý: Cần cài đặt fbprophet hoặc prophet: pip install prophet
try:
    from prophet import Prophet
except ImportError:
    Prophet = None

app = Flask(__name__)

@app.route('/predict', methods=['POST'])
def predict():
    if Prophet is None:
        return jsonify({"error": "Prophet library not installed"}), 500
    
    data = request.json  # Giả sử nhận list of {"ds": "2023-01-01", "y": 100}
    df = pd.DataFrame(data)
    
    model = Prophet()
    model.fit(df)
    
    future = model.make_future_dataframe(periods=30)
    forecast = model.predict(future)
    
    # Trả về kết quả dự báo 30 ngày tới
    result = forecast[['ds', 'yhat']].tail(30).to_dict(orient='records')
    return jsonify(result)

if __name__ == '__main__':
    app.run(port=5000)
