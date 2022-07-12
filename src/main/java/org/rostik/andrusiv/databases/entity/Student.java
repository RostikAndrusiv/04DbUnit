package org.rostik.andrusiv.databases.entity;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Student {

    private int id;

    private String name;

    private String surName;

    private LocalDate dob;

    private int phoneNumber;

    private LocalDateTime created;

    private LocalDateTime updated;

    public Student(String name, String surName, LocalDate dob, int phoneNumber) {
        this.name = name;
        this.surName = surName;
        this.dob = dob;
        this.phoneNumber = phoneNumber;
    }
}
