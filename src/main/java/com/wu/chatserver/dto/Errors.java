package com.wu.chatserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Errors {

    private String message;

    //private List<String> errors = new ArrayList<>();

}
