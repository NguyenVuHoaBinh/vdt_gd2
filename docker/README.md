## Docker Log
- **ELK**
    - Đã thay thế bằng phiên bản ELK của [myshop](https://github.com/NguyenVuHoaBinh/myshop)

    - Trường hợp `[localhost:5601]` Kibana server is not ready yet.
        - Lỗi: `unable to authenticate user [kibana_system] for REST request`

        - Đổi mật khẩu qua `changeme` bằng câu lệnh sau:

            ```angular2html
              docker exec -it elk-elasticsearch-1 bin/elasticsearch-reset-password -u kibana_system -i
            ``` 

            <br>

- **Redis**
    - Đã chỉnh sửa `docker-compose.yaml`
      - Chỉ chạy container cho `redis-vector-db`