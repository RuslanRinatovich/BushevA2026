package com.furniture.controller;

import com.furniture.dto.UserDto;
import com.furniture.entity.User;
import com.furniture.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    // Список ролей
    private static final List<String> ROLE_OPTIONS = Arrays.asList("DIRECTOR", "MASTER", "STOREKEEPER");

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/users/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("user", new UserDto());
        model.addAttribute("roleOptions", ROLE_OPTIONS);
        return "admin/users/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole());
        dto.setEnabled(user.getEnabled());
        model.addAttribute("user", dto);
        model.addAttribute("roleOptions", ROLE_OPTIONS);
        return "admin/users/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute UserDto userDto, RedirectAttributes ra) {
        try {
            User user = new User();
            user.setId(userDto.getId());
            user.setUsername(userDto.getUsername());
            user.setFullName(userDto.getFullName());
            user.setRole(userDto.getRole());
            user.setEnabled(userDto.getEnabled() != null ? userDto.getEnabled() : true);  // Добавить

            if (userDto.getPassword() != null && !userDto.getPassword().isBlank()) {
                user.setPassword(userDto.getPassword());
            }

            userService.save(user);
            ra.addFlashAttribute("success", "Пользователь сохранён");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users/create";
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            userService.deleteById(id);
            ra.addFlashAttribute("success", "Пользователь удалён");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}