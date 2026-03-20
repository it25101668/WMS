package com.wms.controller;

import com.wms.entity.Report;
import com.wms.entity.User;
import com.wms.service.ReportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping
    public String listReports(@RequestParam(required = false) String search,
                              @RequestParam(required = false) String type,
                              HttpSession session,
                              Model model) {
        // Check if admin is logged in
        if (session.getAttribute("adminUser") == null) {
            return "redirect:/login";
        }

        List<Report> reports;
        if (search != null && !search.isBlank()) {
            reports = reportService.searchReports(search);
        } else if (type != null && !type.isBlank()) {
            reports = reportService.getReportsByType(type);
        } else {
            reports = reportService.getAllReports();
        }

        model.addAttribute("reports", reports);
        model.addAttribute("totalReports", reportService.getTotalReports());
        model.addAttribute("search", search);
        model.addAttribute("type", type);
        model.addAttribute("newReport", new Report());
        return "admin/reports";
    }

    @PostMapping("/save")
    public String createReport(@ModelAttribute Report report,
                               HttpSession session,
                               RedirectAttributes ra) {
        User adminUser = (User) session.getAttribute("adminUser");
        if (adminUser == null) {
            return "redirect:/login";
        }

        try {
            // Set the admin who generated the report
            report.setGeneratedBy(adminUser.getFullName() != null ?
                    adminUser.getFullName() : adminUser.getUsername());

            reportService.addReport(report);
            ra.addFlashAttribute("successMsg", "✅ Report created successfully!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", "⚠️ " + e.getMessage());
        }
        return "redirect:/admin/reports";
    }

    @GetMapping("/view/{id}")
    public String viewReport(@PathVariable Long id,
                             HttpSession session,
                             Model model,
                             RedirectAttributes ra) {
        if (session.getAttribute("adminUser") == null) {
            return "redirect:/login";
        }

        return reportService.getReportById(id).map(report -> {
            model.addAttribute("report", report);
            return "admin/report-view";
        }).orElseGet(() -> {
            ra.addFlashAttribute("errorMsg", "⚠️ Report not found.");
            return "redirect:/admin/reports";
        });
    }

    @GetMapping("/delete/{id}")
    public String deleteReport(@PathVariable Long id,
                               HttpSession session,
                               RedirectAttributes ra) {
        if (session.getAttribute("adminUser") == null) {
            return "redirect:/login";
        }

        try {
            reportService.deleteReport(id);
            ra.addFlashAttribute("successMsg", "✅ Report deleted successfully!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", "⚠️ " + e.getMessage());
        }
        return "redirect:/admin/reports";
    }
}