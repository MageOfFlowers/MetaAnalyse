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

    @GetMapping("/deck-infos")
    public List<DeckInfo> getAllDeckInfos() {
        return deckInfoRepo.findAll();
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    @GetMapping("/decks")
    public List<Deck> getAllDecks() {
        return deckRepo.findAll();
    }

    // --- CÁC HÀM CŨ GIỮ NGUYÊN ---

    @PostMapping("/decks")
    public Deck createDeck(@RequestBody DeckRequestDTO dto) {
        return gameService.createDeck(dto);
    }

    @GetMapping("/cards")
    public List<Card> getCards() { return cardRepo.findAll(); }

    @GetMapping("/competitions")
    public List<Competition> getCompetitions() { return compRepo.findAll(); }

    @PostMapping("/users")
    public User addUser(@RequestParam String username) {
        return gameService.createUser(username);
    }

    @PostMapping("/decks")
    public Deck createDeck(@RequestBody DeckRequestDTO dto) {
        return gameService.createDeck(dto);
    }

    @PostMapping("/competition-use")
    public CompetitionUse register(@RequestBody CompetitionRegistrationDTO dto) {
        return gameService.registerToCompetition(dto);
    }
    
    @GetMapping("/competition-use")
    public List<CompetitionUse> getAllCompetitionUses() {
        return compUseRepo.findAll();
    }
}
