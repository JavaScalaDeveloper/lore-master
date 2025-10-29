curl -LO https://ollama.com/download/ollama-linux-amd64.tgz
#假如下载速度慢，则在Windows中用迅雷下载，然后上传到Linux
sudo rm -rf /usr/lib/ollama
sudo tar -C /usr -xzf ollama-linux-amd64.tgz

#启动ollama，可选
ollama serve