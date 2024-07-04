cd /home/ubuntu/s3
sudo docker build -t web-comiper .
sudo docker run -d -p 8080:8080 web-comiper