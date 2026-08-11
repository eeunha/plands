package com.plands.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TodoDeleteTargetDto {
    private Long todoId;
    private Long memberId;
}
