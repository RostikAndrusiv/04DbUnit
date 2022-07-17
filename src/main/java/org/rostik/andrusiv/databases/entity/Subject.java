package org.rostik.andrusiv.databases.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Subject {

    private int id;

    private String subjectName;

    private String tutor;

}
