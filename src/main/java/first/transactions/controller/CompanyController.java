package first.transactions.controller;

import first.transactions.model.Company;
import first.transactions.repository.CompanyRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/companies")
@CrossOrigin("*")
public class CompanyController {
    private final CompanyRepository companyRepository;
    public CompanyController(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;

    }
    @GetMapping
    public List<Company> getCompanies() {
        return companyRepository.findAll();
    }

}
