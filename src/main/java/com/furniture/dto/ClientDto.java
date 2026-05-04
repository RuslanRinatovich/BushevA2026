package com.furniture.dto;

import lombok.Data;

@Data
public class ClientDto {
    private Long id;
    private String name;
    private String inn;
    private String phone;
    private String address;
    private String contactPerson;
}