package com.edutest.test_system.controllers;

import com.edutest.test_system.models.*;
import com.edutest.test_system.repositories.*;
import com.edutest.test_system.util.FileUploadUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class MainController {

    private static final String REDIRECT_LOGIN = "redirect:/login";
    private static final String REDIRECT_INDEX = "redirect:/";
    private static final String REDIRECT_PROFILE = "redirect:/profile";

    private final TestRepository testRepository;
    private final ResultRepository resultRepository; 
    private final QuestionRepository questionRepository; 
    private final UserRepository userRepository; 

    public MainController(TestRepository testRepository, ResultRepository resultRepository, 
                          QuestionRepository questionRepository, UserRepository userRepository) {
        this.testRepository = testRepository;
        this.resultRepository = resultRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
    }

    @ModelAttribute("user")
    public UserEntity globalUser(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email != null) {
            return userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }

    @GetMapping("/")
    public String index(@RequestParam(required = false) String keyword, Model model) {
        List<TestEntity> allTests;
        if (keyword != null && !keyword.trim().isEmpty()) {
            allTests = testRepository.searchPublishedTests(keyword.trim());
        } else {
            allTests = testRepository.findByPublished(true);
        }
        model.addAttribute("allTests", allTests);
        model.addAttribute("keyword", keyword);
        return "index";
    }

    @PostMapping("/join-test")
    public String joinTest(@RequestParam String testCode) {
        if (testCode != null && !testCode.trim().isEmpty()) {
            TestEntity test = testRepository.findByTestCode(testCode.trim());
            if (test != null && !"CLOSED".equals(test.getStatus())) {
                return "redirect:/test/lobby/" + test.getId();
            }
        }
        return "redirect:/?joinError=true";
    }

    @GetMapping("/add-question")
    public String addQuestionPage(HttpSession session) {
        if (session.getAttribute("userEmail") == null) return REDIRECT_LOGIN;
        return "add-question";
    }

    @PostMapping("/add-test-with-questions")
    public String saveTest(HttpServletRequest request,
                           @RequestParam(required = false) String title,
                           @RequestParam(required = false) String category,
                           @RequestParam(required = false) Integer timeLimit,
                           @RequestParam(required = false) String targetAudience,
                           @RequestParam(required = false) String description,
                           @RequestParam(required = false) String published,
                           @RequestParam(required = false) String hideAnswers,
                           HttpSession session) {
        
        String userEmail = (String) session.getAttribute("userEmail");
        if (userEmail == null) return REDIRECT_LOGIN;
        
        TestEntity test = new TestEntity();
        test.setTitle(title); 
        test.setDescription(description); 
        test.setCategory(category);

        test.setTimeLimit((timeLimit != null && timeLimit > 0) ? timeLimit : null);
        
        test.setTargetAudience(targetAudience);
        test.setTestCode(UUID.randomUUID().toString().substring(0, 8));
        test.setAuthorEmail(userEmail); 
        test.setPublished("on".equals(published));
        test.setHideAnswers("on".equals(hideAnswers)); 
        testRepository.save(test);

        Map<String, MultipartFile> fileMap = ((MultipartHttpServletRequest) request).getFileMap();
        
        int questionIndex = 0;
        while (true) {
            String qText = request.getParameter("questionText_" + questionIndex);
            if (qText == null || qText.isEmpty()) break;
            
            QuestionEntity question = new QuestionEntity();
            question.setQuestionText(qText);
            
            String pointsStr = request.getParameter("points_" + questionIndex);
            question.setPoints((pointsStr != null && !pointsStr.isEmpty()) ? Integer.parseInt(pointsStr) : 1);
            
            question.setOptionA(request.getParameter("optionA_" + questionIndex));
            question.setOptionB(request.getParameter("optionB_" + questionIndex));
            question.setOptionC(request.getParameter("optionC_" + questionIndex));
            question.setOptionD(request.getParameter("optionD_" + questionIndex));

            String[] correctAnswersArray = request.getParameterValues("correctAnswers_" + questionIndex);
            String correctAnswerStr = "";
            
            if (correctAnswersArray != null && correctAnswersArray.length > 0) {
                String rawAnswer = String.join(",", correctAnswersArray);
                
                correctAnswerStr = Arrays.stream(rawAnswer.toUpperCase().split(","))
                                         .map(String::trim)
                                         .filter(s -> !s.isEmpty())
                                         .sorted()
                                         .collect(Collectors.joining(","));
            }
            question.setCorrectAnswer(correctAnswerStr);
            // ==================================================

            try {
                MultipartFile imageFile = fileMap.get("imageFile_" + questionIndex);
                if (imageFile != null && !imageFile.isEmpty()) {
                    String uploadDir = "uploads/questions";
                    String fileName = FileUploadUtil.saveFile(uploadDir, imageFile.getOriginalFilename(), imageFile.getInputStream());
                    question.setImageUrl("/uploads/questions/" + fileName);
                } else {
                    String imageUrl = request.getParameter("imageUrl_" + questionIndex);
                    if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                        question.setImageUrl(imageUrl.trim());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            question.setTest(test); 
            questionRepository.save(question);
            questionIndex++; 
        }
        return REDIRECT_PROFILE; 
    }

    @GetMapping("/profile")
    public String showProfile(Model model, HttpSession session) {
        String userEmail = (String) session.getAttribute("userEmail");
        if (userEmail == null) return REDIRECT_LOGIN;
        model.addAttribute("myTests", testRepository.findByAuthorEmail(userEmail));
        model.addAttribute("passedResults", resultRepository.findByStudent(userRepository.findByEmail(userEmail).orElse(null))); 
        return "profile";
    }

    @GetMapping("/test/practice/{id}")
    public String practiceTest(@PathVariable Long id, HttpSession session) {
        session.removeAttribute("studentFirstName");
        session.removeAttribute("studentLastName");
        return "redirect:/test/pass/" + id;
    }

    @GetMapping("/test/lobby/{id}")
    public String testLobby(@PathVariable Long id, Model model, HttpSession session) {
        TestEntity test = testRepository.findById(id).orElse(null);
        if (test == null || "CLOSED".equals(test.getStatus())) return REDIRECT_INDEX;
        
        UserEntity user = (UserEntity) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("prefilledFirstName", user.getFirstName());
            model.addAttribute("prefilledLastName", user.getLastName());
        } else {
            model.addAttribute("prefilledFirstName", "");
            model.addAttribute("prefilledLastName", "");
        }
        
        model.addAttribute("test", test);
        return "test-lobby";
    }

    @PostMapping("/test/start-session/{id}")
    public String startTestSession(@PathVariable Long id, 
                                   @RequestParam String firstName, 
                                   @RequestParam String lastName, 
                                   HttpSession session) {
        TestEntity test = testRepository.findById(id).orElse(null);
        if (test == null || "CLOSED".equals(test.getStatus())) return REDIRECT_INDEX;

        session.setAttribute("studentFirstName", firstName);
        session.setAttribute("studentLastName", lastName);
        
        if ("WAITING".equals(test.getStatus())) {
            ResultEntity lobbyResult = new ResultEntity();
            lobbyResult.setStudentFirstName(firstName);
            lobbyResult.setStudentLastName(lastName);
            lobbyResult.setTest(test);
            lobbyResult.setPercentage(-1.0); 
            resultRepository.save(lobbyResult);
            return "redirect:/test/waiting/" + id; 
        }
        
        return "redirect:/test/pass/" + id;
    }

    @GetMapping("/test/waiting/{id}")
    public String waitingRoom(@PathVariable Long id, Model model) {
        model.addAttribute("testId", id);
        return "test-waiting";
    }

    @GetMapping("/test/pass/{id}")
    public String passTestPage(@PathVariable Long id, Model model) {
        TestEntity test = testRepository.findById(id).orElse(null);
        if (test == null) return REDIRECT_INDEX;
        model.addAttribute("test", test);
        return "test-pass"; 
    }

    @GetMapping("/api/tests/{id}/questions")
    @ResponseBody
    public List<QuestionEntity> getQuestions(@PathVariable Long id) {
        return questionRepository.findByTestId(id); 
    }

    @PostMapping("/test/{id}/submit")
    public String submitTest(@PathVariable Long id, HttpServletRequest request, HttpSession session, Model model) {
        TestEntity test = testRepository.findById(id).orElse(null);
        if (test == null) return REDIRECT_INDEX;

        List<QuestionEntity> questions = test.getQuestions();
        if (questions == null) questions = new ArrayList<>();

        int totalQuestions = questions.size();
        int correctCount = 0;
        int studentPoints = 0;
        int maxPoints = 0;


        for (QuestionEntity q : questions) {
            maxPoints += q.getPoints(); 
            String studentAnswer = request.getParameter("question_" + q.getId());
            String correctAnswer = q.getCorrectAnswer();
            
            if (studentAnswer != null && correctAnswer != null && !correctAnswer.isEmpty()) {
                String normalizedStudentAnswer = Arrays.stream(studentAnswer.toUpperCase().split(","))
                                                        .map(String::trim)
                                                        .filter(s -> !s.isEmpty())
                                                        .sorted()
                                                        .collect(Collectors.joining(","));
                                                        
                if (normalizedStudentAnswer.equals(correctAnswer)) {
                    correctCount++;
                    studentPoints += q.getPoints();
                }
            }
        }


        double percentage = (maxPoints > 0) ? Math.round(((double) studentPoints / maxPoints) * 100.0) : 0;

        String firstName = (String) session.getAttribute("studentFirstName");
        if (firstName != null && !firstName.isEmpty()) {
            
            List<ResultEntity> existingResults = resultRepository.findByTestIdAndPercentage(id, -1.0);
            ResultEntity resultToUpdate = null;
            
            for (ResultEntity r : existingResults) {
                if (firstName.equals(r.getStudentFirstName()) && 
                    ((String)session.getAttribute("studentLastName")).equals(r.getStudentLastName())) {
                    resultToUpdate = r;
                    break;
                }
            }

            if (resultToUpdate != null) {
                resultToUpdate.setPercentage(percentage);
                resultRepository.save(resultToUpdate);
            } else {
                ResultEntity result = new ResultEntity();
                String userEmail = (String) session.getAttribute("userEmail");
                if (userEmail != null) {
                    UserEntity user = userRepository.findByEmail(userEmail).orElse(null);
                    result.setStudent(user);
                }
                result.setStudentFirstName(firstName);
                result.setStudentLastName((String) session.getAttribute("studentLastName"));
                result.setTest(test);
                result.setPercentage(percentage);
                resultRepository.save(result);
            }
        }

        model.addAttribute("testTitle", test.getTitle());
        model.addAttribute("percentage", percentage);
        model.addAttribute("correctCount", correctCount);
        model.addAttribute("totalQuestions", totalQuestions);
        model.addAttribute("studentPoints", studentPoints);
        model.addAttribute("maxPoints", maxPoints);

        return "test-result"; 
    }

    @GetMapping("/test/teacher-results/{id}")
    public String showTeacherResults(@PathVariable Long id, Model model, HttpSession session) {
        String userEmail = (String) session.getAttribute("userEmail");
        TestEntity test = testRepository.findById(id).orElse(null);
        
        if (test == null || userEmail == null) return REDIRECT_INDEX;
        if (!userEmail.equals(test.getAuthorEmail())) return REDIRECT_INDEX;

        List<ResultEntity> results = resultRepository.findByTestIdOrderByPercentageDesc(id);
        
        model.addAttribute("test", test);
        model.addAttribute("results", results);
        return "teacher-results";
    }

    @GetMapping("/test/preview/{id}")
    public String previewTest(@PathVariable Long id, Model model, HttpSession session) {
        TestEntity test = testRepository.findById(id).orElse(null);
        if (test == null) return REDIRECT_INDEX;
        
        model.addAttribute("test", test);
        return "test-preview";
    }

    @PostMapping("/test/conduct/{id}")
    public String conductTest(@PathVariable Long id, HttpSession session) {
        String userEmail = (String) session.getAttribute("userEmail");
        if (userEmail == null) return REDIRECT_LOGIN;

        TestEntity originalTest = testRepository.findById(id).orElse(null);
        if (originalTest == null) return REDIRECT_INDEX;

        TestEntity newTestSession = new TestEntity();
        newTestSession.setTitle(originalTest.getTitle() + " (Сесія)");
        newTestSession.setDescription(originalTest.getDescription());
        newTestSession.setCategory(originalTest.getCategory());
        newTestSession.setTimeLimit(originalTest.getTimeLimit());
        newTestSession.setTargetAudience(originalTest.getTargetAudience());
        newTestSession.setHideAnswers(originalTest.isHideAnswers());
        newTestSession.setAuthorEmail(userEmail); 
        newTestSession.setTestCode(UUID.randomUUID().toString().substring(0, 8)); 
        newTestSession.setPublished(false);
        newTestSession.setStatus("WAITING"); 
        testRepository.save(newTestSession);

        List<QuestionEntity> originalQuestions = originalTest.getQuestions();
        if (originalQuestions != null) {
            for (QuestionEntity originalQ : originalQuestions) {
                QuestionEntity newQ = new QuestionEntity();
                newQ.setQuestionText(originalQ.getQuestionText());
                newQ.setOptionA(originalQ.getOptionA());
                newQ.setOptionB(originalQ.getOptionB());
                newQ.setOptionC(originalQ.getOptionC());
                newQ.setOptionD(originalQ.getOptionD());
                newQ.setCorrectAnswer(originalQ.getCorrectAnswer());
                newQ.setPoints(originalQ.getPoints());
                newQ.setImageUrl(originalQ.getImageUrl());
                newQ.setVideoUrl(originalQ.getVideoUrl());
                newQ.setTest(newTestSession);
                questionRepository.save(newQ);
            }
        }

        return "redirect:/test/teacher-results/" + newTestSession.getId();
    }

    @GetMapping("/api/tests/{id}/status")
    @ResponseBody
    public String getTestStatus(@PathVariable Long id) {
        TestEntity test = testRepository.findById(id).orElse(null);
        return (test != null) ? test.getStatus() : "CLOSED";
    }

    @GetMapping("/api/tests/{id}/lobby-users")
    @ResponseBody
    public List<ResultEntity> getLobbyUsers(@PathVariable Long id) {
        return resultRepository.findByTestIdAndPercentage(id, -1.0);
    }

    @GetMapping("/api/tests/{id}/results")
    @ResponseBody
    public List<ResultEntity> getTestResults(@PathVariable Long id) {
        return resultRepository.findByTestIdOrderByPercentageDesc(id);
    }

    @PostMapping("/test/start-teacher/{id}")
    public String startTeacherTest(@PathVariable Long id, HttpSession session) {
        TestEntity test = testRepository.findById(id).orElse(null);
        if (test != null) {
            test.setStatus("IN_PROGRESS");
            testRepository.save(test);
        }
        return "redirect:/test/teacher-results/" + id;
    }

    @PostMapping("/test/close-teacher/{id}")
    public String closeTeacherTest(@PathVariable Long id, HttpSession session) {
        TestEntity test = testRepository.findById(id).orElse(null);
        if (test != null) {
            test.setStatus("CLOSED");
            testRepository.save(test);
        }
        return "redirect:/test/teacher-results/" + id;
    }

    @GetMapping("/account")
    public String showAccountPage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return REDIRECT_LOGIN;
        
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        model.addAttribute("accountUser", user);
        return "account";
    }

    @PostMapping("/account/update")
    public String updateAccount(@RequestParam String firstName, 
                                @RequestParam String lastName,
                                @RequestParam String institution,
                                @RequestParam(required = false) MultipartFile avatar,
                                @RequestParam(required = false) String oldPassword,
                                @RequestParam(required = false) String newPassword,
                                HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return REDIRECT_LOGIN;
        
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return REDIRECT_LOGIN;

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setInstitution(institution);
        
        if (avatar != null && !avatar.isEmpty()) {
            try {
                String uploadDir = "uploads/avatars";
                String fileName = FileUploadUtil.saveFile(uploadDir, avatar.getOriginalFilename(), avatar.getInputStream());
                user.setAvatarUrl("/uploads/avatars/" + fileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (oldPassword != null && !oldPassword.isEmpty() && newPassword != null && !newPassword.isEmpty()) {
            if (user.getPassword().equals(oldPassword)) {
                user.setPassword(newPassword);
            } else {
                return "redirect:/account?passwordError=true";
            }
        }

        userRepository.save(user);
        return "redirect:/account?success=true";
    }

    @PostMapping("/test/delete/{id}")
    public String deleteTest(@PathVariable Long id, HttpSession session) {
        String userEmail = (String) session.getAttribute("userEmail");
        TestEntity test = testRepository.findById(id).orElse(null);
        if (userEmail != null && test != null && userEmail.equals(test.getAuthorEmail())) {
            testRepository.delete(test);
        }
        return REDIRECT_PROFILE; 
    }
}