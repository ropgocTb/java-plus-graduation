package ru.practicum.stats.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.stats.dto.StatRequestDto;
import ru.practicum.stats.dto.StatResponseDto;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "stats-server")
public interface StatsClient {

    @PostMapping("/hit")
    void hit(@RequestBody StatRequestDto dto);

    @GetMapping("/stats")
    List<StatResponseDto> getStats( @RequestParam("start")
                                    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                    @RequestParam("end")
                                    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                    @RequestParam(value = "uris", required = false) List<String> uris,
                                    @RequestParam("unique") boolean unique);
}
