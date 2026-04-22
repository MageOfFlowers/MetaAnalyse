package api;

import dto.*;
import model.*;
import service.CardGameService;
import repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST})
public class GameController {

    @Autowired private CardGameService gameService;
    @Autowired private CardRepository cardRepo;
    @Autowired private CompetitionRepository compRepo;
    @Autowired private CompetitionUseRepository compUseRepo;
    @Autowired private DeckInfoRepository deckInfoRepo;
    
    // ĐÃ THÊM: Khai báo 2 repository thiếu để sửa lỗi "cannot find symbol"
    @Autowired private UserRepository userRepo;
    @Autowired private DeckRepository deckRepo;

    // --- LẤY DỮ LIỆU (GET) ---

    @GetMapping("/cards")
    public List<Card> getCards() { 
        return cardRepo.findAll(); 
    }

    @GetMapping("/competitions")
    public List<Competition> getCompetitions() { 
        return compRepo.findAll(); 
    }

    @GetMapping("/competition-use")
    public List<CompetitionUse> getAllCompetitionUses() {
        return compUseRepo.findAll();
    }

    @GetMapping("/deck-infos")
    public List<DeckInfo> getAllDeckInfos() {
        return deckInfoRepo.findAll();
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    @GetMapping("/decksget")
    public List<Deck> getAllDecks() {
        return deckRepo.findAll();
    }

    // --- TẠO DỮ LIỆU (POST) ---

    @PostMapping("/usersadd")
    public User addUser(@RequestParam String username) {
        return gameService.createUser(username);
    }

    // ĐÃ GỘP: Chỉ giữ lại MỘT hàm createDeck duy nhất
    @PostMapping("/decks")
    public Deck createDeck(@RequestBody DeckRequestDTO dto) {
        return gameService.createDeck(dto);
    }

    @PostMapping("/competition-use")
    public CompetitionUse register(@RequestBody CompetitionRegistrationDTO dto) {
        return gameService.registerToCompetition(dto);
    }
    
    @PostMapping("/competition-use-delete")
    public int deleteCompetitionUse(@RequestBody long Id) {
        return gameService.deleteCompetitionUse(Id);
    }
    
    @PostMapping("/deck-delete")
    public int deleteDeck(@RequestBody long Id) {
        return gameService.deleteDeck(Id);
    }
    
    @PostMapping("/user-delete")
    public int deleteUser(@RequestBody long Id) {
        return gameService.deleteUser(Id);
    }
}
