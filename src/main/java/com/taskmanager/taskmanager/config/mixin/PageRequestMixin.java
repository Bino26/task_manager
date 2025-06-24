package com.taskmanager.taskmanager.config.mixin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Sort;

public abstract class PageRequestMixin {

    @JsonCreator
    public PageRequestMixin(
            @JsonProperty("page") int page,
            @JsonProperty("size") int size,
            @JsonProperty("sort") Sort sort
    ) {
    }
}
