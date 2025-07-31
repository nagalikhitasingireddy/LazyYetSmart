import whisper
import sys

# Load Whisper model (base is fast & accurate enough)
model = whisper.load_model("base")

# Get file path from command-line argument
audio_path = sys.argv[1]

# Transcribe the file
result = model.transcribe(audio_path)

# Print the result text so Java can capture it
print(result["text"])
