package net.java.lms_backend.Service;

import net.java.lms_backend.Repositrory.CourseRepository;
import net.java.lms_backend.Repositrory.QuizAttemptRepository;
import net.java.lms_backend.Repositrory.QuizRepository;
import net.java.lms_backend.Repositrory.StudentRepository;
import net.java.lms_backend.dto.QuizDTO;
import net.java.lms_backend.entity.Course;
import net.java.lms_backend.entity.Question;
import net.java.lms_backend.entity.Quiz;
import net.java.lms_backend.entity.QuizAttempt;
import net.java.lms_backend.entity.Student;
import net.java.lms_backend.mapper.QuizMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizService {
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;

    public QuizService(QuizRepository quizRepository,
                       QuizAttemptRepository quizAttemptRepository,
                       CourseRepository courseRepository,
                       StudentRepository studentRepository) {
        this.quizRepository = quizRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
    }

    public Quiz createQuiz(Long courseId, QuizDTO quizDTO) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found"));
        Quiz quiz = QuizMapper.toEntity(quizDTO);
        quiz.setCourse(course);
        return quizRepository.save(quiz);
    }

    public QuizAttempt generateQuizAttempt(Long quizId, Long studentId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new NoSuchElementException("Quiz not found"));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NoSuchElementException("Student not found"));

        List<Question> questions = quiz.getCourse().getQuestionsBank();

        List<Question> mcqQuestions = getRandomQuestions(questions, "MCQ", quiz.getNumOfMCQ());
        List<Question> trueFalseQuestions = getRandomQuestions(questions, "TRUE_FALSE", quiz.getNumOfTrueFalse());
        List<Question> shortAnswerQuestions = getRandomQuestions(questions, "SHORT_ANSWER", quiz.getNumOfShortAnswer());

        List<Question> selectedQuestions = new ArrayList<>();
        selectedQuestions.addAll(mcqQuestions);
        selectedQuestions.addAll(trueFalseQuestions);
        selectedQuestions.addAll(shortAnswerQuestions);

        QuizAttempt quizAttempt = new QuizAttempt();
        quizAttempt.setQuiz(quiz);
        quizAttempt.setStudent(student);
        quizAttempt.setQuestions(selectedQuestions);

        return quizAttemptRepository.save(quizAttempt);
    }

    private List<Question> getRandomQuestions(List<Question> questions, String type, Long count) {
        List<Question> filteredQuestions = questions.stream()
                .filter(q -> q.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());

        if (filteredQuestions.size() < count) {
            throw new IllegalArgumentException("Not enough questions of type: " + type);
        }

        Collections.shuffle(filteredQuestions);
        return filteredQuestions.subList(0, Math.toIntExact(count));
    }

    public int updateQuizAttempt(Long quizAttemptId, Map<Long, String> answers) {
        QuizAttempt quizAttempt = quizAttemptRepository.findById(quizAttemptId)
                .orElseThrow(() -> new NoSuchElementException("Quiz attempt not found"));

        quizAttempt.setAnswers(answers);

        int score = 0;
        for (Map.Entry<Long, String> entry : answers.entrySet()) {
            Long questionId = entry.getKey();
            String studentAnswer = entry.getValue();

            Question question = quizAttempt.getQuestions().stream()
                    .filter(q -> q.getId().equals(questionId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid question ID: " + questionId));

            if (question.getCorrectAnswer().equalsIgnoreCase(studentAnswer)) {
                score++;
            }
        }

        quizAttempt.setScore(score);
        quizAttemptRepository.save(quizAttempt);

        return score;
    }

    public QuizAttempt getQuizAttempt(Long quizAttemptId) {
        return quizAttemptRepository.findById(quizAttemptId)
                .orElseThrow(() -> new NoSuchElementException("Quiz attempt not found"));
    }

    public double getAverageScoreByQuizId(Long quizId) {
        List<QuizAttempt> quizAttempts = quizAttemptRepository.findByQuizId(quizId);
        if (quizAttempts.isEmpty()) return 0.0;

        int totalScore = quizAttempts.stream().mapToInt(QuizAttempt::getScore).sum();
        return (double) totalScore / quizAttempts.size();
    }

    public List<QuizAttempt> getQuizAttemptsByStudent(Long studentId, long courseId) {
        return quizAttemptRepository.findByStudentIdAndQuiz_Course_Id(studentId, courseId);
    }

    public Double getAverageScoreOfStudent(Long studentId, long courseId) {
        List<QuizAttempt> quizAttempts = quizAttemptRepository.findByStudentIdAndQuiz_Course_Id(studentId, courseId);
        return quizAttempts.stream()
                .mapToDouble(QuizAttempt::getScore)
                .average()
                .orElse(0.0);
    }
}
