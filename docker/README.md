## Docker Log
- **ELK**
    - [09/01/2025] Thay thế bằng phiên bản ELK của [myshop](https://github.com/NguyenVuHoaBinh/myshop)
    - [09/01/2025] Đổi mật khẩu qua Kibana qua `changeme` nếu gặp
        - [ Lỗi ]: localhost:5601 Kibana server is not ready yet.
        - [ Lỗi ]: unable to authenticate user "kibana_system" for REST request

          ```
            docker exec -it elk-elasticsearch-1 bin/elasticsearch-reset-password -u kibana_system -i
          ``` 

- **Redis**
    - [09/01/2025] Cho ẩn node-1 đến node 6 trong `docker-compose.yaml` do chưa sử dụng tới
    - [18/01/2025] Gắn redis-insight vô redis-network trong `docker-compose.yaml` do không thể kết nối tới redis-insight

    - **Redis Insight**
      ```
        Host: redis-vector-db
        Port: 6379
        Password: hoabinh12
      ```

- **Kafka**
    - Chưa sử dụng tới     