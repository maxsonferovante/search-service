import http.server
import socketserver
import json
import random
from datetime import datetime, timedelta
from urllib.parse import urlparse, parse_qs

# Dados de exemplo baseados nos seus INSERTs para usar como default ou inspiração
mock_routes_inspiration = [
    {"id": 1, "alert_id": 101, "origin": "GRU", "destination": "MIA", "outbound_date": "2025-08-15", "return_date": "2025-08-25", "target_price": 2500.00, "tolerance_up": 100.00, "currency": "BRL", "active": True},
    {"id": 2, "alert_id": 102, "origin": "GIG", "destination": "LIS", "outbound_date": "2025-09-10", "return_date": "2025-09-20", "target_price": 3200.50, "tolerance_up": 150.00, "currency": "BRL", "active": True},
    {"id": 3, "alert_id": 103, "origin": "SSA", "destination": "JFK", "outbound_date": "2025-10-01", "return_date": "2025-10-12", "target_price": 2800.75, "tolerance_up": 50.25, "currency": "USD", "active": True},
]

def generate_flight_leg_detail(departure_id, arrival_id, base_date_str, is_return_leg=False):
    """Gera detalhes para um trecho de voo."""
    try:
        dep_time_obj_base = datetime.strptime(base_date_str, "%Y-%m-%d")
    except ValueError:
        # Fallback se a data não estiver no formato esperado, use hoje + alguns dias
        dep_time_obj_base = datetime.now() + timedelta(days=random.randint(30,90))


    if is_return_leg:
        dep_time_obj_base += timedelta(days=random.randint(1,5))

    departure_time_str = dep_time_obj_base.replace(hour=random.randint(6, 18), minute=random.randint(0,59)).strftime("%Y-%m-%dT%H:%M")
    duration_minutes = random.randint(60, 720) # 1 a 12 horas
    arrival_time_obj = datetime.strptime(departure_time_str, "%Y-%m-%dT%H:%M") + timedelta(minutes=duration_minutes)
    arrival_time_str = arrival_time_obj.strftime("%Y-%m-%dT%H:%M")

    return {
        "departure_airport": {
            "name": f"{departure_id} Airport Name",
            "id": departure_id,
            "time": departure_time_str
        },
        "arrival_airport": {
            "name": f"{arrival_id} Airport Name",
            "id": arrival_id,
            "time": arrival_time_str
        },
        "duration": duration_minutes,
        "airplane": random.choice(["Boeing 737", "Airbus A320", "Boeing 777", "Embraer E195"]),
        "airline": random.choice(["MockAir", "FlyCheap", "SkyPath", "TravelMock"]),
        "airline_logo": f"https://logo.clearbit.com/{random.choice(['aa.com','latam.com','gol.com.br','flytap.com'])}?size=40",
        "travel_class": random.choice(["Economy", "Business", "First"]),
        "flight_number": f"{random.choice(['MKA', 'FLC', 'SKP', 'TMK'])}{random.randint(100, 9999)}",
        "extensions": random.sample(["WiFi", "Meal included", "Extra legroom", "Refundable"], k=random.randint(0, 3)),
        "ticket_also_sold_by": [],
        "legroom": f"{random.randint(28, 36)} inches",
        "overnight": random.choice([True, False]),
        "often_delayed_by_over_30_min": random.choice([True, False, False]),
        "plane_and_crew_by": "Mock Operating Carrier"
    }

def generate_flight_option(params):
    """Gera uma opção de voo completa."""
    flight_legs = []
    layovers = []
    total_flight_duration = 0

    leg1_departure_id = params["departure_id"]
    leg1_arrival_id = params["arrival_id"]

    if random.choice([True, True, False]):
        leg1 = generate_flight_leg_detail(leg1_departure_id, leg1_arrival_id, params["outbound_date"])
        flight_legs.append(leg1)
        total_flight_duration += leg1["duration"]
    else:
        layover_airport_id = f"L{random.choice(['AY','XY','WZ'])}"
        leg1_part1 = generate_flight_leg_detail(leg1_departure_id, layover_airport_id, params["outbound_date"])
        flight_legs.append(leg1_part1)
        total_flight_duration += leg1_part1["duration"]

        layover_duration_minutes = random.randint(45, 240)
        layovers.append({
            "duration": layover_duration_minutes,
            "name": f"{layover_airport_id} Layover Airport",
            "id": layover_airport_id,
            "overnight": random.choice([True, False])
        })
        total_flight_duration += layover_duration_minutes

        prev_arrival_dt = datetime.strptime(leg1_part1["arrival_airport"]["time"], "%Y-%m-%dT%H:%M")
        next_departure_base_dt = prev_arrival_dt + timedelta(minutes=layover_duration_minutes)

        leg1_part2 = generate_flight_leg_detail(layover_airport_id, leg1_arrival_id, next_departure_base_dt.strftime("%Y-%m-%d"))
        leg1_part2["departure_airport"]["time"] = (next_departure_base_dt + timedelta(minutes=random.randint(5,30))).strftime("%Y-%m-%dT%H:%M")
        new_departure_dt_p2 = datetime.strptime(leg1_part2["departure_airport"]["time"], "%Y-%m-%dT%H:%M")
        leg1_part2["arrival_airport"]["time"] = (new_departure_dt_p2 + timedelta(minutes=leg1_part2["duration"])).strftime("%Y-%m-%dT%H:%M")

        flight_legs.append(leg1_part2)
        total_flight_duration += leg1_part2["duration"]

    if params.get("return_date"):
        last_outbound_arrival_dt = datetime.strptime(flight_legs[-1]["arrival_airport"]["time"], "%Y-%m-%dT%H:%M")
        base_return_dt_param = datetime.strptime(params["return_date"], "%Y-%m-%d")
        actual_base_return_dt = max(last_outbound_arrival_dt.date() + timedelta(days=1), base_return_dt_param.date())

        leg2 = generate_flight_leg_detail(params["arrival_id"], params["departure_id"], actual_base_return_dt.strftime("%Y-%m-%d"), is_return_leg=True)
        flight_legs.append(leg2)
        total_flight_duration += leg2["duration"]

    return {
        "flights": flight_legs,
        "layovers": layovers,
        "total_duration": total_flight_duration,
        "carbon_emissions": {
            "this_flight": random.randint(100, 1000),
            "typical_for_this_route": random.randint(150, 1200),
            "difference_percent": random.randint(-30, 30)
        },
        "price": random.randint(150, 3500), # PREÇO ALEATÓRIO
        "type": "Round trip" if params.get("return_date") else "One-way",
        "airline_logo": f"https://logo.clearbit.com/{random.choice(['expedia.com','booking.com','kayak.com'])}?size=50",
        "extensions": ["Cheapest option", "Recommended"] if random.random() > 0.7 else [],
        "departure_token": f"dep_token_{random.getrandbits(32)}",
        "booking_token": f"book_token_{random.getrandbits(32)}"
    }

class MockFlightAPIHandler(http.server.BaseHTTPRequestHandler):
    def do_GET(self):
        parsed_url = urlparse(self.path)
        path = parsed_url.path
        query_params_dict = parse_qs(parsed_url.query)

        if path == '/search_flights':
            default_route_data = random.choice(mock_routes_inspiration)

            params = {
                "departure_id": query_params_dict.get('departure_id', [default_route_data['origin']])[0],
                "arrival_id": query_params_dict.get('arrival_id', [default_route_data['destination']])[0],
                "outbound_date": query_params_dict.get('outbound_date', [default_route_data['outbound_date']])[0],
                "currency": query_params_dict.get('currency', [default_route_data['currency']])[0],
            }
            # Lidar com return_date que pode não estar presente
            return_date_list = query_params_dict.get('return_date')
            if return_date_list:
                params["return_date"] = return_date_list[0]
            elif default_route_data.get("return_date") and not return_date_list: # Se não veio na query mas tem no default
                 params["return_date"] = default_route_data["return_date"]
            else: # Se não veio na query e nem no default
                params["return_date"] = None


            response_data_payload = {
                "search_parameters": {
                    "departure_id": params["departure_id"],
                    "arrival_id": params["arrival_id"],
                    "outbound_date": params["outbound_date"],
                    "return_date": params["return_date"],
                    "currency": params["currency"]
                },
                "other_flights": []
            }

            if random.choice([True, True, False]):
                response_data_payload["best_flights"] = [generate_flight_option(params) for _ in range(random.randint(1, 3))]

            num_other_flights = random.randint(1,5) if "best_flights" not in response_data_payload else random.randint(0,2)
            for _ in range(num_other_flights):
                response_data_payload["other_flights"].append(generate_flight_option(params))

            if "best_flights" not in response_data_payload and not response_data_payload["other_flights"]:
                 response_data_payload["other_flights"] = [generate_flight_option(params) for _ in range(random.randint(2, 4))]

            # Enviar resposta
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*') # Para testes de browser (CORS)
            self.end_headers()
            self.wfile.write(json.dumps(response_data_payload).encode('utf-8'))
        else:
            self.send_response(404)
            self.send_header('Content-type', 'text/plain')
            self.end_headers()
            self.wfile.write(b"Not Found. Use /search_flights endpoint.")

def run_server(port=5002):
    server_address = ('', port)
    httpd = http.server.HTTPServer(server_address, MockFlightAPIHandler)
    print(f"Servidor mock de voos rodando na porta {port}...")
    print(f"Tente: http://localhost:{port}/search_flights?departure_id=GRU&arrival_id=MIA&outbound_date=2025-07-10&return_date=2025-07-20")
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\nServidor parando...")
        httpd.server_close()

if __name__ == '__main__':
    run_server()