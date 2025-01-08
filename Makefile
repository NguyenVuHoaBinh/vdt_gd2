.PHONY: docker-start
.PHONY: docker-stop
.PHONY: docker-restart

DOCKER_DIR = docker

# or docker.bat
DOCKER_SCRIPT = docker.sh

docker-start:
	@cd $(DOCKER_DIR) && chmod +x $(DOCKER_SCRIPT) && sh $(DOCKER_SCRIPT) start && cd ..

docker-stop:
	@cd $(DOCKER_DIR) && chmod +x $(DOCKER_SCRIPT) && sh $(DOCKER_SCRIPT) stop && cd ..

docker-restart:
	@cd $(DOCKER_DIR) && chmod +x $(DOCKER_SCRIPT) && sh $(DOCKER_SCRIPT) restart && cd ..