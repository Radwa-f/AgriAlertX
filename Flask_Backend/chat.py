from flask import Flask, request, jsonify
from transformers import pipeline
import requests
import json
import os
import re


app = Flask(__name__)

# Define the API key for the Gemini API.
# It is highly recommended to use environment variables for keys in a production setting.
# Example: export GEMINI_API_KEY='your_api_key_here'
GEMINI_API_KEY = "AIzaSyBA-mi-6fbQo2lZiY5owuiD8KurKpWd9vg" # Replace with your actual Gemini API Key if not using environment variable

# Define the API route
@app.route('/chat', methods=['POST'])
def chat():
    user_query = request.json.get('query')  # Get the query from the request
    if not user_query:
        return jsonify({'error': 'No query provided'}), 400

    response_text = ""

    
    # If the primary model is not loaded or failed to respond, use the Gemini API
    if not response_text:
        print("Using Gemini API as a failover...")
        try:
            # --- Gemini API Call ---
            # Updated prompt with explicit instructions for plain text output
            prompt = f"You are an agricultural assistant chatbot. Help a farmer take care of their crops optimally, be concise and on point, no too long texts. Answer the following question in a single paragraph, without using any formatting, bold text, or lists: {user_query}"
            
            # The API endpoint for the Gemini 2.5 Flash model
            apiUrl = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-05-20:generateContent?key={GEMINI_API_KEY}"
            
            payload = {
                "contents": [
                    {
                        "role": "user",
                        "parts": [{"text": prompt}]
                    }
                ],
                "generationConfig": {
                    "responseMimeType": "text/plain"  # This ensures the response is plain text
                }
            }
            
            headers = {
                "Content-Type": "application/json"
            }
            
            # Make the API call to Gemini
            gemini_response = requests.post(apiUrl, headers=headers, data=json.dumps(payload))
            gemini_response.raise_for_status() # Raise an error for bad status codes
            
            result = gemini_response.json()
            
            # Extract the generated text from the Gemini response
            if 'candidates' in result and result['candidates']:
                response_text = result['candidates'][0]['content']['parts'][0]['text']

                # --- Post-processing step to remove stubborn markdown formatting ---
                # Remove markdown bold/italic characters
                response_text = re.sub(r'(\*\*|\*|__|_)', '', response_text)
                # Remove common list-like characters at the start of a line
                response_text = re.sub(r'^\s*[\d\.\-]+\s*', '', response_text, flags=re.MULTILINE)
                
            else:
                response_text = "Sorry, I am unable to generate a response at this time."
            
            print("Gemini API response generated.")
        except requests.exceptions.RequestException as req_e:
            print(f"Error calling Gemini API: {req_e}")
            response_text = "An error occurred while trying to get a response from the failover system."
        except Exception as e:
            print(f"An unexpected error occurred during failover: {e}")
            response_text = "An unexpected error occurred. Please try again later."
    
    # Return the final response as JSON
    return jsonify({'response': response_text})

# Ensure safe importing
if __name__ == "__main__":
    app.run(debug=True, use_reloader=False)

