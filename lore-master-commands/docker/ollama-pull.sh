docker pull ollama/ollama
docker run -d -v ollama:/root/.ollama -p 11434:11434 --name ollama ollama/ollama


docker exec -it ollama bash

ollama run qwen3:0.6b