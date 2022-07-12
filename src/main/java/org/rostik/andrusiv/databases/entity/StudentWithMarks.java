package org.rostik.andrusiv.databases.entity;

import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
public class StudentWithMarks extends Student {
    List<ExamResult> examResultList = new ArrayList<>();

    public StudentWithMarks(int id, String name, String surName, LocalDate dob, int phoneNumber, LocalDateTime created, LocalDateTime updated) {
        super(id, name, surName, dob, phoneNumber, created, updated);
    }

    public List<ExamResult> getExamResultList() {
        return examResultList;
    }
}
