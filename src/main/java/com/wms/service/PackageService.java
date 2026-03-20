package com.wms.service;

import com.wms.entity.WeddingPackage;
import com.wms.repository.PackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PackageService {

    @Autowired
    private PackageRepository packageRepository;

    public List<WeddingPackage> getAllPackages() {
        return packageRepository.findAll();
    }

    public Optional<WeddingPackage> getPackageById(Long id) {
        return packageRepository.findById(id);
    }

    public WeddingPackage addPackage(WeddingPackage weddingPackage) {
        return packageRepository.save(weddingPackage);
    }

    public WeddingPackage updatePackage(Long id, WeddingPackage updatedPackage) {
        WeddingPackage existing = packageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found with id: " + id));
        existing.setName(updatedPackage.getName());
        existing.setDescription(updatedPackage.getDescription());
        existing.setPrice(updatedPackage.getPrice());
        existing.setDuration(updatedPackage.getDuration());
        existing.setInclusions(updatedPackage.getInclusions());
        existing.setMaxGuests(updatedPackage.getMaxGuests());
        existing.setStatus(updatedPackage.getStatus());
        return packageRepository.save(existing);
    }

    public void deletePackage(Long id) {
        if (!packageRepository.existsById(id)) {
            throw new RuntimeException("Package not found with id: " + id);
        }
        packageRepository.deleteById(id);
    }

    public List<WeddingPackage> searchPackages(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return packageRepository.findAll();
        }
        return packageRepository.searchPackages(keyword.trim());
    }

    public List<WeddingPackage> getAvailablePackages() {
        return packageRepository.findByStatus("Available");
    }

    public long getTotalPackages() {
        return packageRepository.count();
    }
}
