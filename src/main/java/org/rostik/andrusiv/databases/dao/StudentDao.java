package org.rostik.andrusiv.databases.dao;

import org.rostik.andrusiv.databases.entity.Student;
import org.rostik.andrusiv.databases.entity.StudentWithMarks;

import java.util.List;

public interface StudentDao extends GenericDao<Student, Integer> {

    List<Student> findStudentByNameExact(String firstName, String lastName);

    List<Student> findStudentByLastNamePartial(String surname);

    List<Student> findStudentByPhonePartial(int phone);

    List<StudentWithMarks> findStudentWithMarksByLastNamePartial(String surname);
}
