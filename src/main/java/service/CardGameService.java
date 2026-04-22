package service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dto.DeckRequestDTO;
import dto.CompetitionRegistrationDTO;
import model.*;
import repository.*;

@Service
public class CardGameService {

    @Autowired private CardRepository cardRepo;
    @Autowired private DeckRepository deckRepo;
    @Autowired private DeckInfoRepository deckInfoRepo;
    @Autowired private CompetitionUseRepository compUseRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private CompetitionRepository compRepo;

    // --- 1. LẤY DANH SÁCH ---
    public List<Competition> getAllCompetitions() { return compRepo.findAll(); }
    public List<User> getAllUsers() { return userRepo.findAll(); }
    public List<Card> getAllCards() { return cardRepo.findAll(); }
    
    // Lấy danh sách Deck kèm theo chi tiết lá bài (DeckInfo)
    public List<Deck> getAllDecksWithInfo() {
        return deckRepo.findAll();
    }

    // --- 2. TẠO USER ---
    public User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        // Bạn có thể set name mặc định trùng username hoặc để trống
        user.setName(username); 
        return userRepo.save(user);
    }

    // --- 3. TẠO DECK (Gồm Deck và DeckInfo) ---
    @Transactional
    public Deck createDeck(DeckRequestDTO dto) {
        // Tạo entry trong bảng Deck
        Deck deck = new Deck();
        deck.setName(dto.getName());
        Deck savedDeck = deckRepo.save(deck);

        // Tạo các entry trong bảng DeckInfo liên kết với Deck vừa tạo
        if (dto.getCards() != null) {
            for (DeckRequestDTO.CardItem item : dto.getCards()) {
                DeckInfo info = new DeckInfo();
                info.setDeckid(savedDeck.getId());
                info.setCardid(item.cardid);
                info.setQuantity(item.quantity);
                deckInfoRepo.save(info);
            }
        }
        return savedDeck;
    }

    // --- 4. XÁC NHẬN SỬ DỤNG DECK TRONG GIẢI ---
    public CompetitionUse registerToCompetition(CompetitionRegistrationDTO dto) {
        CompetitionUse use = new CompetitionUse();
        use.setDeckid(dto.getDeckid());
        use.setCompetitionid(dto.getCompetitionid());
        use.setUserid(dto.getUserid());
        use.setWinrate(dto.getWinrate());
        use.setRank(dto.getRank());
        return compUseRepo.save(use);
    }
    
    public int deleteCompetitionUse(Long id) {
        if (compUseRepo.existsById(id)) {
            compUseRepo.deleteById(id);
            return 0;
        } else {
            return 1;
        }
    }
    
    public int deleteDeck(Long id) {
        if (deckRepo.existsById(id)) {
            deckRepo.deleteById(id);
            return 0;
        } else {
            return 1;
        }
    }
    
    
    public int deleteUser(Long id) {
        if (userRepo.existsById(id)) {
            userRepo.deleteById(id);
            return 0;
        } else {
            return 1;
        }
    }
}