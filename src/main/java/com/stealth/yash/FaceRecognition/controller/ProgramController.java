package com.stealth.yash.FaceRecognition.controller;


import com.stealth.yash.FaceRecognition.model.Course;
import com.stealth.yash.FaceRecognition.model.Professor;
import com.stealth.yash.FaceRecognition.model.Program;
import com.stealth.yash.FaceRecognition.model.Student;
import com.stealth.yash.FaceRecognition.service.springdatajpa.CourseSDJpaService;
import com.stealth.yash.FaceRecognition.service.springdatajpa.DepartmentSDJpaService;
import com.stealth.yash.FaceRecognition.service.springdatajpa.ProfessorSDJpaService;
import com.stealth.yash.FaceRecognition.service.springdatajpa.ProgramSDJpaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

@Slf4j
@Controller
@RequestMapping("/programs")
public class ProgramController {

    private final ProgramSDJpaService programSDJpaService;
    private final DepartmentSDJpaService departmentSDJpaService;
    private final ProfessorSDJpaService professorSDJpaService;
    private final CourseSDJpaService courseSDJpaService;

    public ProgramController(ProgramSDJpaService programSDJpaService, DepartmentSDJpaService departmentSDJpaService, ProfessorSDJpaService professorSDJpaService, CourseSDJpaService courseSDJpaService) {
        this.programSDJpaService = programSDJpaService;
        this.departmentSDJpaService = departmentSDJpaService;
        this.professorSDJpaService = professorSDJpaService;
        this.courseSDJpaService = courseSDJpaService;
    }

    @GetMapping({"", "/"})
    public String getPrograms(Model model, @RequestParam(value = "value", required = false, defaultValue = "") String val) {

        if (val != null && !val.trim().isEmpty()) {
            List<Program> program = programSDJpaService.searchProgram(val);
            model.addAttribute("programs", programSDJpaService.searchProgram(val));
        } else {
            model.addAttribute("programs", programSDJpaService.findAll());
        }

        return "program/programs";
    }

    @GetMapping("/get/{programId}")
    public String showProgramInfo(@PathVariable Long programId, Model model) {

        model.addAttribute("program", programSDJpaService.findById(programId));
        return "program/program-info";
    }

    @GetMapping({"/update/{programId}", "/create"})
    public String createOrUpdateProgram(@PathVariable(required = false) Long programId, Model model) {
        if (programId != null) {
            model.addAttribute("program", programSDJpaService.findById(programId));
        } else {
            Program program = new Program();
            model.addAttribute("program", program);
        }

        model.addAttribute("departments", departmentSDJpaService.findAll());

        return "program/createOrUpdateProgram";
    }

    @PostMapping("")
    public String processUpdateProgramForm(@Valid @ModelAttribute("program") Program program, BindingResult bindingResult, Model model) {


        if (bindingResult.hasErrors()){
            bindingResult.getAllErrors().forEach(error -> log.error(error.toString()));
            model.addAttribute("departments", departmentSDJpaService.findAll());
            return "professor/createOrUpdateProfessor";
        }


        Program program1 = programSDJpaService.save(program);

        return "redirect:/programs/get/" + program1.getId();
    }

    @GetMapping("/delete/{programId}")
    public String deleteProgram(@PathVariable Long programId) {

        Set<Professor> professors = professorSDJpaService.findProfessorsByProgramId(programId);
        for (Professor professor : professors){
            professor.setProgram(null);
        }
        Set<Course> courses = courseSDJpaService.findCoursesByProgramId(programId);
        for (Course course : courses){
            course.setProgram(null);
        }

        programSDJpaService.deleteById(programId);

        return "redirect:/programs";
    }

    @GetMapping("/by-departmentId")
    @ResponseBody
    public Set<Program> getProgramsByDepartmentId(@RequestParam Long departmentId) {
        return programSDJpaService.findProgramByDepartmentId(departmentId);
    }

}
