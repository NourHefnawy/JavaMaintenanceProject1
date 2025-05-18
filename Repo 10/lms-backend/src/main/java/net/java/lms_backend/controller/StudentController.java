package net.java.lms_backend.controller;

import net.java.lms_backend.dto.StudentDTO;
import net.java.lms_backend.dto.SubmissionDTO;
import net.java.lms_backend.service.StudentService;
import net.java.lms_backend.service.SubmissionService;
import net.java.lms_backend.mapper.SubmissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentService studentService;
    private final SubmissionService submissionService;
    private final SubmissionMapper submissionMapper;

    @Autowired
    public StudentController(StudentService studentService,
                             SubmissionService submissionService,
                             SubmissionMapper submissionMapper) {
        this.studentService = studentService;
        this.submissionService = submissionService;
        this.submissionMapper = submissionMapper;
    }

    //  Submit assignment
    @PostMapping("/{assignmentId}/submit")
    public ResponseEntity<SubmissionDTO> createSubmission(
            @PathVariable Long assignmentId,
            @RequestParam Long studentId,
            @RequestParam("file") MultipartFile file) {
        SubmissionDTO createdSubmission = submissionService.createSubmission(assignmentId, studentId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSubmission);
    }

    //  Get all submissions
    @GetMapping("/submissions")
    public ResponseEntity<List<SubmissionDTO>> getAllSubmissions() {
        List<SubmissionDTO> submissions = submissionService.getAllSubmissions();
        return ResponseEntity.ok(submissions);
    }

    //  Get submission by ID
    @GetMapping("/submissions/{id}")
    public ResponseEntity<SubmissionDTO> getSubmissionById(@PathVariable Long id) {
        SubmissionDTO submission = submissionService.getSubmissionById(id);
        if (submission != null) {
            return ResponseEntity.ok(submission);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    //  Delete submission by ID
    @DeleteMapping("/submissions/{id}")
    public ResponseEntity<Void> deleteSubmission(@PathVariable Long id) {
        boolean isDeleted = submissionService.deleteSubmission(id);
        if (isDeleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    //  Get all students
    @GetMapping
    public ResponseEntity<List<StudentDTO>> getAllStudents() {
        List<StudentDTO> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    //  Get student by ID
    @GetMapping("/{id}")
    public ResponseEntity<StudentDTO> getStudentById(@PathVariable Long id) {
        StudentDTO studentDTO = studentService.getStudentById(id);
        if (studentDTO != null) {
            return ResponseEntity.ok(studentDTO);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    //  Create new student
    @PostMapping
    public ResponseEntity<StudentDTO> createStudent(@RequestBody StudentDTO studentDTO) {
        StudentDTO createdStudent = studentService.createStudent(studentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
    }

    // Update existing student
    @PutMapping("/{id}")
    public ResponseEntity<StudentDTO> updateStudent(@PathVariable Long id, @RequestBody StudentDTO studentDTO) {
        StudentDTO updatedStudent = studentService.updateStudent(id, studentDTO);
        if (updatedStudent != null) {
            return ResponseEntity.ok(updatedStudent);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    //  Delete student
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        boolean isDeleted = studentService.deleteStudent(id);
        if (isDeleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
