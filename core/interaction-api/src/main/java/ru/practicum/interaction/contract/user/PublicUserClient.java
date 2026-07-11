package ru.practicum.interaction.contract.user;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "user-service", contextId = "publicUserClient", path = "/users")
public interface PublicUserClient extends PublicUserOperations {
}
