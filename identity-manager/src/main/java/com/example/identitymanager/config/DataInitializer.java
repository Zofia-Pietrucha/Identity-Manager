package com.example.identitymanager.config;

import com.example.identitymanager.model.Role;
import com.example.identitymanager.model.SupportTicket;
import com.example.identitymanager.model.User;
import com.example.identitymanager.repository.RoleRepository;
import com.example.identitymanager.repository.SupportTicketRepository;
import com.example.identitymanager.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository,
                                   RoleRepository roleRepository,
                                   SupportTicketRepository ticketRepository,
                                   PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if data already exists
            if (roleRepository.count() > 0) {
                return; // Data already initialized
            }

            // Create roles
            Role userRole = new Role();
            userRole.setName(Role.RoleName.USER);
            roleRepository.save(userRole);

            Role adminRole = new Role();
            adminRole.setName(Role.RoleName.ADMIN);
            roleRepository.save(adminRole);

            // Create admin user
            User admin = new User();
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("password123"));
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setPhone("123456789");
            admin.setIsPrivacyEnabled(false);
            admin.getRoles().add(adminRole);
            admin.getRoles().add(userRole);
            userRepository.save(admin);

            // Create regular user 1
            User john = new User();
            john.setEmail("john@example.com");
            john.setPassword(passwordEncoder.encode("password123"));
            john.setFirstName("John");
            john.setLastName("Doe");
            john.setPhone("987654321");
            john.setIsPrivacyEnabled(true);
            john.getRoles().add(userRole);
            userRepository.save(john);

            // Create regular user 2
            User jane = new User();
            jane.setEmail("jane@example.com");
            jane.setPassword(passwordEncoder.encode("password123"));
            jane.setFirstName("Jane");
            jane.setLastName("Smith");
            jane.setIsPrivacyEnabled(false);
            jane.getRoles().add(userRole);
            userRepository.save(jane);

            // Create support tickets
            SupportTicket ticket1 = new SupportTicket();
            ticket1.setSubject("Cannot login");
            ticket1.setDescription("I forgot my password and cannot login to my account");
            ticket1.setStatus(SupportTicket.TicketStatus.OPEN);
            ticket1.setUser(john);
            ticketRepository.save(ticket1);

            SupportTicket ticket2 = new SupportTicket();
            ticket2.setSubject("Profile update issue");
            ticket2.setDescription("When I try to update my profile, I get an error");
            ticket2.setStatus(SupportTicket.TicketStatus.IN_PROGRESS);
            ticket2.setUser(john);
            ticketRepository.save(ticket2);

            SupportTicket ticket3 = new SupportTicket();
            ticket3.setSubject("Privacy settings");
            ticket3.setDescription("How do I enable privacy settings?");
            ticket3.setStatus(SupportTicket.TicketStatus.RESOLVED);
            ticket3.setUser(jane);
            ticketRepository.save(ticket3);

            System.out.println("Database initialized with sample data!");
        };
    }
}