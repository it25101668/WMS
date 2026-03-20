package com.wms.controller;

import com.wms.entity.WeddingPackage;
import com.wms.service.PackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/packages")
public class PackageController {

    @Autowired
    private PackageService packageService;

    @GetMapping
    public String listPackages(@RequestParam(value = "search", required = false) String search, Model model) {
        if (search != null && !search.isEmpty()) {
            model.addAttribute("packages", packageService.searchPackages(search));
            model.addAttribute("search", search);
        } else {
            model.addAttribute("packages", packageService.getAllPackages());
        }
        model.addAttribute("totalPackages", packageService.getTotalPackages());
        return "admin/packages";
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("pkg", new WeddingPackage());
        model.addAttribute("formTitle", "Add New Package");
        model.addAttribute("formAction", "/admin/packages/save");
        return "admin/package-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return packageService.getPackageById(id).map(pkg -> {
            model.addAttribute("pkg", pkg);
            model.addAttribute("formTitle", "Edit Package");
            model.addAttribute("formAction", "/admin/packages/update/" + id);
            return "admin/package-form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("errorMsg", "Package not found!");
            return "redirect:/admin/packages";
        });
    }

    @PostMapping("/save")
    public String savePackage(@ModelAttribute WeddingPackage weddingPackage, RedirectAttributes ra) {
        try {
            packageService.addPackage(weddingPackage);
            ra.addFlashAttribute("successMsg", "Package created successfully!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/packages";
    }

    @PostMapping("/update/{id}")
    public String updatePackage(@PathVariable Long id, @ModelAttribute WeddingPackage weddingPackage, RedirectAttributes ra) {
        try {
            packageService.updatePackage(id, weddingPackage);
            ra.addFlashAttribute("successMsg", "Package updated successfully!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/packages";
    }

    @GetMapping("/delete/{id}")
    public String deletePackage(@PathVariable Long id, RedirectAttributes ra) {
        try {
            packageService.deletePackage(id);
            ra.addFlashAttribute("successMsg", "Package deleted successfully!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/packages";
    }
}
