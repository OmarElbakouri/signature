package com.signature.service;

import com.signature.entity.User;
import com.signature.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + username));
    }
    
    public User registerUser(User user) throws Exception {
        // Vérifier si l'utilisateur existe déjà
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new Exception("Ce nom d'utilisateur est déjà utilisé");
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new Exception("Cette adresse email est déjà utilisée");
        }
        
        // Encoder le mot de passe
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Sauvegarder l'utilisateur
        return userRepository.save(user);
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * Met à jour les informations d'un utilisateur
     * @param user L'utilisateur avec les informations mises à jour
     * @return L'utilisateur mis à jour
     */
    @Transactional
    public User updateUser(User user) throws Exception {
        // Vérifier si l'email est déjà utilisé par un autre utilisateur
        Optional<User> existingUserWithEmail = findByEmail(user.getEmail());
        if (existingUserWithEmail.isPresent() && !existingUserWithEmail.get().getId().equals(user.getId())) {
            throw new Exception("Cette adresse email est déjà utilisée par un autre utilisateur");
        }
        
        return userRepository.save(user);
    }
    
    /**
     * Met à jour le mot de passe d'un utilisateur
     * @param user L'utilisateur concerné
     * @param currentPassword Le mot de passe actuel
     * @param newPassword Le nouveau mot de passe
     * @return true si le mot de passe a été mis à jour, false sinon
     */
    @Transactional
    public boolean updatePassword(User user, String currentPassword, String newPassword) throws Exception {
        // Vérifier que le mot de passe actuel est correct
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }
        
        // Encoder et enregistrer le nouveau mot de passe
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }
}
