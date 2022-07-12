package org.rostik.andrusiv.databases.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExamResult {

    private int studentId;

    private int subjectId;

    private int mark;
}
