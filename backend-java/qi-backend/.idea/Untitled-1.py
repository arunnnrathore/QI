"""
THE ULTIMATE GK CHALLENGE
==========================
An original, KBC-inspired General Knowledge quiz game built with Python's
built-in Tkinter library. No external packages are required.

Run it with:
    python game.py

HOW TO ADD / EDIT QUESTIONS:
    Scroll down to the QUESTION_BANK dictionary below. Questions are grouped
    into 6 difficulty "tiers" (1 = easiest, 6 = hardest). To add a question,
    copy an existing dictionary inside the tier list you want and edit the
    "question", "options" and "explanation" values. The first item in the
    "options" list must always be the CORRECT answer -- the game shuffles
    the display order automatically, so the answer letter (A/B/C/D) changes
    every time you play.
"""

import random
import tkinter as tk
from tkinter import messagebox

# Sound effects are OPTIONAL. winsound only exists on Windows, so we try to
# import it and quietly fall back to "no sound" everywhere else. The game
# works perfectly fine without any sound at all.
try:
    import winsound
    SOUND_AVAILABLE = True
except ImportError:
    SOUND_AVAILABLE = False


# =====================================================================
#  COLORS & FONTS  (change these to re-theme the whole game)
# =====================================================================
BG_MAIN = "#100833"        # deep navy/purple background
BG_HEADER = "#170b45"
BG_PANEL = "#1d1054"
GOLD = "#FFCC33"
ACCENT_CYAN = "#3FD0FF"
WHITE = "#F2F2F5"
GREEN = "#2ECC71"
RED = "#E74C3C"
DIM_TEXT = "#A9A6C4"

BTN_NORMAL = "#2E1B6B"
BTN_HOVER = "#4A2F9E"
BTN_SELECTED = "#FFB800"
BTN_DISABLED = "#3A3550"

LIFELINE_BG = "#1D6FD1"
LIFELINE_HOVER = "#2E86F0"
LIFELINE_USED_BG = "#3A3550"

WALK_BG = "#8B1E3F"
WALK_HOVER = "#B0294F"

FONT_TITLE = ("Helvetica", 40, "bold")
FONT_SUBTITLE = ("Helvetica", 22, "bold")
FONT_TAGLINE = ("Helvetica", 14, "italic")
FONT_HEADER = ("Helvetica", 16, "bold")
FONT_QUESTION = ("Helvetica", 17, "bold")
FONT_OPTION = ("Helvetica", 13, "bold")
FONT_BUTTON = ("Helvetica", 11, "bold")
FONT_MENU_BUTTON = ("Helvetica", 13, "bold")
FONT_LADDER = ("Helvetica", 11, "bold")
FONT_LADDER_CURRENT = ("Helvetica", 12, "bold")
FONT_SMALL = ("Helvetica", 10)
FONT_HINT = ("Helvetica", 12, "italic")
FONT_BIG_MONEY = ("Helvetica", 24, "bold")

OPTION_LETTERS = ["A", "B", "C", "D"]


# =====================================================================
#  PRIZE LADDER
# =====================================================================
PRIZE_LEVELS = [
    1000, 2000, 3000, 5000, 10000,
    20000, 40000, 80000, 160000, 320000,
    640000, 1250000, 2500000, 5000000, 10000000,
]

# Indices (0-based) of the "safe haven" levels. If a player answers wrong
# AFTER passing one of these, they keep that amount instead of losing
# everything.
SAFE_HAVEN_INDICES = {4, 9}   # -> Rs 10,000 and Rs 3,20,000

# Which difficulty tier (1-6) each of the 15 questions belongs to.
LEVEL_TIER = [1, 1, 1, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 6, 6]

CORRECT_PHRASES = [
    "Brilliant! That's correct!",
    "Well done -- correct answer!",
    "Excellent! You got it right!",
    "Correct! Keep going!",
    "Nailed it!",
]


def format_inr(amount):
    """Format a number using the Indian digit-grouping style, e.g. 1000000
    becomes '10,00,000'. Returns just the digits/commas (no currency sign)."""
    s = str(amount)
    if len(s) <= 3:
        return s
    last3 = s[-3:]
    rest = s[:-3]
    parts = []
    while len(rest) > 2:
        parts.insert(0, rest[-2:])
        rest = rest[:-2]
    if rest:
        parts.insert(0, rest)
    return ",".join(parts) + "," + last3


# =====================================================================
#  QUESTION BANK  (58 questions across 6 difficulty tiers)
#  In every "options" list, item [0] is always the CORRECT answer.
#  The game shuffles the order before showing it to the player.
# =====================================================================
QUESTION_BANK = {

    # ---------------- TIER 1 : VERY EASY ----------------
    1: [
        {"question": "What is the capital city of India?",
         "options": ["New Delhi", "Mumbai", "Kolkata", "Chennai"],
         "explanation": "New Delhi has served as the capital of India since 1911."},
        {"question": "Which is the largest planet in our solar system?",
         "options": ["Jupiter", "Saturn", "Earth", "Neptune"],
         "explanation": "Jupiter is the largest planet, with a mass more than twice that of all other planets combined."},
        {"question": "How many continents are there on Earth?",
         "options": ["7", "5", "6", "8"],
         "explanation": "Earth is traditionally divided into seven continents."},
        {"question": "What is the chemical formula of water?",
         "options": ["H2O", "CO2", "NaCl", "O2"],
         "explanation": "Water consists of two hydrogen atoms and one oxygen atom."},
        {"question": "Which animal is the national animal of India?",
         "options": ["Bengal Tiger", "Lion", "Elephant", "Peacock"],
         "explanation": "The Bengal Tiger was declared India's national animal in 1973 under Project Tiger."},
        {"question": "How many colors are traditionally seen in a rainbow?",
         "options": ["7", "5", "6", "8"],
         "explanation": "A rainbow traditionally shows seven colors, remembered as VIBGYOR."},
        {"question": "Which is the fastest land animal in the world?",
         "options": ["Cheetah", "Lion", "Horse", "Leopard"],
         "explanation": "The cheetah can reach speeds of up to 100-120 km/h."},
        {"question": "In which direction does the sun rise?",
         "options": ["East", "West", "North", "South"],
         "explanation": "Because of Earth's rotation, the sun always appears to rise in the east."},
        {"question": "Which organ in the human body pumps blood?",
         "options": ["Heart", "Lungs", "Liver", "Kidney"],
         "explanation": "The heart is a muscular organ that pumps blood through the circulatory system."},
        {"question": "Which is the largest ocean in the world?",
         "options": ["Pacific Ocean", "Atlantic Ocean", "Indian Ocean", "Arctic Ocean"],
         "explanation": "The Pacific Ocean is the largest and deepest of the world's oceans."},
        {"question": "India is often referred to as the world's largest what?",
         "options": ["Democracy", "Monarchy", "Dictatorship", "Federation of city-states"],
         "explanation": "With its huge population and electorate, India is widely called the world's largest democracy."},
    ],

    # ---------------- TIER 2 : EASY ----------------
    2: [
        {"question": "What is the currency of Japan called?",
         "options": ["Yen", "Won", "Yuan", "Ringgit"],
         "explanation": "The Japanese Yen is the official currency of Japan."},
        {"question": "Who is the author of the Harry Potter book series?",
         "options": ["J.K. Rowling", "J.R.R. Tolkien", "Roald Dahl", "C.S. Lewis"],
         "explanation": "British author J.K. Rowling wrote the seven-book Harry Potter series."},
        {"question": "Who was the first person to walk on the Moon?",
         "options": ["Neil Armstrong", "Buzz Aldrin", "Yuri Gagarin", "Michael Collins"],
         "explanation": "Neil Armstrong stepped onto the Moon on 20 July 1969 during Apollo 11."},
        {"question": "Which is the longest river in the world?",
         "options": ["Nile", "Amazon", "Yangtze", "Mississippi"],
         "explanation": "The Nile, flowing through north-eastern Africa, is traditionally called the world's longest river."},
        {"question": "How many players are there in a cricket team on the field at one time?",
         "options": ["11", "10", "9", "12"],
         "explanation": "Each cricket team fields 11 players at a time."},
        {"question": "Which gas do plants absorb from the air for photosynthesis?",
         "options": ["Carbon dioxide", "Oxygen", "Nitrogen", "Hydrogen"],
         "explanation": "Plants absorb carbon dioxide and, using sunlight, produce glucose and oxygen."},
        {"question": "'Bollywood', India's Hindi-language film industry, is based in which city?",
         "options": ["Mumbai", "Delhi", "Chennai", "Kolkata"],
         "explanation": "Bollywood is centered in Mumbai, formerly known as Bombay."},
        {"question": "Which is the smallest prime number?",
         "options": ["2", "1", "0", "3"],
         "explanation": "2 is the smallest prime number and the only even prime number."},
        {"question": "Who is credited with inventing the telephone?",
         "options": ["Alexander Graham Bell", "Thomas Edison", "Nikola Tesla", "Guglielmo Marconi"],
         "explanation": "Alexander Graham Bell was awarded the first US patent for the telephone in 1876."},
    ],

    # ---------------- TIER 3 : MEDIUM ----------------
    3: [
        {"question": "How many Fundamental Rights does the Indian Constitution currently guarantee?",
         "options": ["6", "7", "5", "8"],
         "explanation": "The Constitution originally listed 7 Fundamental Rights, but the Right to Property was removed by the 44th Amendment, leaving 6."},
        {"question": "Who wrote India's national anthem, 'Jana Gana Mana'?",
         "options": ["Rabindranath Tagore", "Bankim Chandra Chattopadhyay", "Sarojini Naidu", "Muhammad Iqbal"],
         "explanation": "Nobel laureate Rabindranath Tagore composed 'Jana Gana Mana'."},
        {"question": "In which year did World War II end?",
         "options": ["1945", "1944", "1946", "1939"],
         "explanation": "World War II ended in 1945 with the surrender of Germany and then Japan."},
        {"question": "Which planet is known as the 'Red Planet'?",
         "options": ["Mars", "Venus", "Mercury", "Jupiter"],
         "explanation": "Mars appears reddish because of iron oxide (rust) covering its surface."},
        {"question": "Who was the first Indian to travel into space?",
         "options": ["Rakesh Sharma", "Kalpana Chawla", "Sunita Williams", "Vikram Sarabhai"],
         "explanation": "Rakesh Sharma flew aboard Soyuz T-11 in 1984, becoming the first Indian in space."},
        {"question": "Which gas makes up the largest portion of Earth's atmosphere?",
         "options": ["Nitrogen", "Oxygen", "Carbon dioxide", "Argon"],
         "explanation": "Nitrogen makes up about 78% of Earth's atmosphere; oxygen is about 21%."},
        {"question": "Who is often called the 'Father of Computers'?",
         "options": ["Charles Babbage", "Alan Turing", "Bill Gates", "Tim Berners-Lee"],
         "explanation": "Charles Babbage designed the Analytical Engine, an early mechanical computer concept."},
        {"question": "The Great Wall, one of the world's most famous landmarks, is located in which country?",
         "options": ["China", "Japan", "Mongolia", "South Korea"],
         "explanation": "The Great Wall of China was built over centuries to protect Chinese states from invasion."},
        {"question": "Which Indian state is popularly known as the 'Spice Garden of India'?",
         "options": ["Kerala", "Goa", "Tamil Nadu", "Karnataka"],
         "explanation": "Kerala has long been famous for producing spices like pepper, cardamom and cloves."},
        {"question": "The Nobel Prize was established in memory of whom?",
         "options": ["Alfred Nobel", "Albert Einstein", "Henry Dunant", "Marie Curie"],
         "explanation": "Swedish inventor Alfred Nobel established the Nobel Prize in his will."},
    ],

    # ---------------- TIER 4 : MODERATE-HARD ----------------
    4: [
        {"question": "Which Article of the Indian Constitution guarantees the 'Right to Equality before Law'?",
         "options": ["Article 14", "Article 19", "Article 21", "Article 32"],
         "explanation": "Article 14 guarantees equality before the law and equal protection of the laws."},
        {"question": "Who was the first President of India?",
         "options": ["Dr. Rajendra Prasad", "Jawaharlal Nehru", "Dr. S. Radhakrishnan", "Zakir Husain"],
         "explanation": "Dr. Rajendra Prasad served as India's first President from 1950 to 1962."},
        {"question": "What is the approximate speed of light in a vacuum?",
         "options": ["3 x 10^8 m/s", "3 x 10^6 m/s", "3 x 10^10 m/s", "3 x 10^5 m/s"],
         "explanation": "Light travels at roughly 299,792 km/s, close to 3 x 10^8 m/s, in a vacuum."},
        {"question": "Which country has the most time zones due to its overseas territories?",
         "options": ["France", "Russia", "United States", "United Kingdom"],
         "explanation": "Counting overseas territories, France spans 12 time zones, more than any other country."},
        {"question": "The 'Rupiah' is the official currency of which country?",
         "options": ["Indonesia", "Malaysia", "Philippines", "Thailand"],
         "explanation": "The Indonesian Rupiah has been Indonesia's official currency since 1949."},
        {"question": "Who discovered the antibiotic penicillin?",
         "options": ["Alexander Fleming", "Louis Pasteur", "Robert Koch", "Jonas Salk"],
         "explanation": "Alexander Fleming discovered penicillin in 1928."},
        {"question": "Exposure to sunlight helps the human body produce which vitamin?",
         "options": ["Vitamin D", "Vitamin C", "Vitamin A", "Vitamin B12"],
         "explanation": "Sunlight triggers the skin to synthesize Vitamin D, important for bone health."},
        {"question": "On which date did the Constitution of India come into effect?",
         "options": ["26 January 1950", "15 August 1947", "26 November 1949", "2 October 1950"],
         "explanation": "The Constitution was adopted on 26 November 1949 but came into force on 26 January 1950, celebrated as Republic Day."},
        {"question": "Who wrote the book 'The Discovery of India'?",
         "options": ["Jawaharlal Nehru", "Mahatma Gandhi", "Sardar Patel", "Subhas Chandra Bose"],
         "explanation": "Jawaharlal Nehru wrote 'The Discovery of India' while imprisoned in the 1940s."},
        {"question": "Which is the smallest country in the world by area?",
         "options": ["Vatican City", "Monaco", "San Marino", "Liechtenstein"],
         "explanation": "Vatican City, enclaved within Rome, covers just about 0.44 square kilometers."},
        {"question": "Who is credited with inventing the first commercially practical incandescent light bulb?",
         "options": ["Thomas Edison", "Nikola Tesla", "James Watt", "Michael Faraday"],
         "explanation": "Thomas Edison developed a long-lasting, commercially viable incandescent bulb in 1879."},
    ],

    # ---------------- TIER 5 : HARD ----------------
    5: [
        {"question": "The 42nd Amendment (1976) added which two words to the Preamble of the Indian Constitution?",
         "options": ["Socialist and Secular", "Sovereign and Democratic", "Republic and Federal", "Justice and Liberty"],
         "explanation": "The 42nd Amendment inserted 'Socialist' and 'Secular' into the Preamble."},
        {"question": "Who became the first woman Prime Minister in modern world history?",
         "options": ["Sirimavo Bandaranaike", "Indira Gandhi", "Golda Meir", "Margaret Thatcher"],
         "explanation": "Sirimavo Bandaranaike of Sri Lanka became the world's first female Prime Minister in 1960."},
        {"question": "Which subatomic particle, sometimes called the 'God particle', was confirmed by CERN in 2012?",
         "options": ["Higgs boson", "Neutrino", "Quark", "Positron"],
         "explanation": "CERN's Large Hadron Collider confirmed the Higgs boson in 2012."},
        {"question": "Who was the first woman to win a Nobel Prize?",
         "options": ["Marie Curie", "Mother Teresa", "Rosalind Franklin", "Toni Morrison"],
         "explanation": "Marie Curie won the Nobel Prize in Physics in 1903, and a second Nobel in Chemistry in 1911."},
        {"question": "Which Indian mathematician, largely self-taught, famously collaborated with G.H. Hardy?",
         "options": ["Srinivasa Ramanujan", "C.V. Raman", "Homi Bhabha", "S. Chandrasekhar"],
         "explanation": "Srinivasa Ramanujan made extraordinary contributions to number theory while working with Hardy at Cambridge."},
        {"question": "What is a group of crows collectively called?",
         "options": ["A murder", "A flock", "A parliament", "A troop"],
         "explanation": "A group of crows is traditionally called 'a murder of crows'."},
        {"question": "Iran was formerly known by which name before 1935?",
         "options": ["Persia", "Mesopotamia", "Babylonia", "Anatolia"],
         "explanation": "The country was officially called Persia internationally until it requested to be called Iran in 1935."},
        {"question": "Which Article of the Indian Constitution abolishes untouchability?",
         "options": ["Article 17", "Article 15", "Article 21", "Article 25"],
         "explanation": "Article 17 abolishes 'untouchability' and forbids its practice in any form."},
        {"question": "Which Indian athlete is popularly known as the 'Flying Sikh'?",
         "options": ["Milkha Singh", "P.T. Usha", "Kapil Dev", "Balbir Singh Sr."],
         "explanation": "Sprinter Milkha Singh earned the nickname 'Flying Sikh' for his remarkable speed."},
    ],

    # ---------------- TIER 6 : VERY HARD ----------------
    6: [
        {"question": "Who was the first Chief Justice of India?",
         "options": ["H. J. Kania", "M. Hidayatullah", "Y.V. Chandrachud", "P.N. Bhagwati"],
         "explanation": "Justice H. J. Kania served as the first Chief Justice of India from 1950 to 1951."},
        {"question": "Which Indian state has the longest coastline?",
         "options": ["Gujarat", "Tamil Nadu", "Andhra Pradesh", "Kerala"],
         "explanation": "Gujarat has India's longest coastline, stretching roughly 1,600 kilometers."},
        {"question": "What was the name of the lander module of India's Chandrayaan-3 mission?",
         "options": ["Vikram", "Pragyan", "Aditya", "Mangalyaan"],
         "explanation": "The Chandrayaan-3 lander, named after ISRO founder Vikram Sarabhai, soft-landed near the Moon's south pole in 2023."},
        {"question": "Who first proposed the theory of continental drift?",
         "options": ["Alfred Wegener", "Charles Darwin", "Isaac Newton", "James Hutton"],
         "explanation": "German scientist Alfred Wegener proposed continental drift theory in 1912."},
        {"question": "What is the SI unit of electric capacitance?",
         "options": ["Farad", "Henry", "Ohm", "Tesla"],
         "explanation": "The farad, named after Michael Faraday, measures electric capacitance."},
        {"question": "Which is the only country to have used nuclear weapons in warfare?",
         "options": ["United States", "Soviet Union", "Germany", "United Kingdom"],
         "explanation": "The United States dropped atomic bombs on Hiroshima and Nagasaki in August 1945."},
        {"question": "The 1955 Bandung Conference is regarded as an important precursor to which movement?",
         "options": ["Non-Aligned Movement", "European Union", "United Nations", "G7"],
         "explanation": "The Bandung Conference of Asian and African states helped lay the groundwork for the Non-Aligned Movement."},
        {"question": "Who is considered the youngest person to become Prime Minister of India?",
         "options": ["Rajiv Gandhi", "Atal Bihari Vajpayee", "Rahul Gandhi", "Chandra Shekhar"],
         "explanation": "Rajiv Gandhi became Prime Minister in 1984 at the age of 40."},
    ],
}


# =====================================================================
#  MAIN GAME CLASS
# =====================================================================
class GKChallengeGame:
    """Controls every screen and every rule of The Ultimate GK Challenge."""

    def __init__(self, root):
        self.root = root
        self.root.title("The Ultimate GK Challenge")
        self.root.configure(bg=BG_MAIN)
        self.root.minsize(900, 650)
        self.center_window()

        # A single container frame holds whichever "screen" is active.
        # Switching screens simply clears and rebuilds this frame.
        self.container = tk.Frame(root, bg=BG_MAIN)
        self.container.pack(fill="both", expand=True)

        self.reset_state()
        self.create_start_screen()

    # -----------------------------------------------------------------
    #  WINDOW HELPERS
    # -----------------------------------------------------------------
    def center_window(self):
        """Centers the game window on the user's screen."""
        self.root.update_idletasks()
        width, height = 1080, 740
        screen_w = self.root.winfo_screenwidth()
        screen_h = self.root.winfo_screenheight()
        x = (screen_w // 2) - (width // 2)
        y = (screen_h // 2) - (height // 2)
        self.root.geometry(f"{width}x{height}+{x}+{y}")

    def clear_container(self):
        """Removes every widget currently on screen before drawing a new one."""
        for widget in self.container.winfo_children():
            widget.destroy()

    def add_hover(self, widget, normal_bg, hover_bg):
        """Gives a button a simple hover (mouse-over) color change."""
        def on_enter(event):
            if str(widget["state"]) != "disabled":
                widget.configure(bg=hover_bg)

        def on_leave(event):
            if str(widget["state"]) != "disabled":
                widget.configure(bg=normal_bg)

        widget.bind("<Enter>", on_enter)
        widget.bind("<Leave>", on_leave)

    def make_menu_button(self, parent, text, command, width=22, bg=BTN_NORMAL, hover=BTN_HOVER):
        """Creates a styled button used on the start/how-to-play/game-over screens."""
        btn = tk.Button(
            parent, text=text, font=FONT_MENU_BUTTON, bg=bg, fg=WHITE,
            activebackground=hover, activeforeground=WHITE, bd=0,
            width=width, pady=10, cursor="hand2", command=command,
        )
        self.add_hover(btn, bg, hover)
        return btn

    def play_sound(self, event):
        """Plays a short optional beep. Silently does nothing if sound
        isn't available on this system -- the game never depends on it."""
        if not SOUND_AVAILABLE:
            return
        try:
            sounds = {
                "select": (700, 80), "correct": (1000, 150), "wrong": (300, 300),
                "lifeline": (900, 100), "victory": (1200, 400), "walk": (600, 150),
            }
            freq, dur = sounds.get(event, (700, 80))
            winsound.Beep(freq, dur)
        except Exception:
            pass  # Never let a sound problem crash the game.

    # -----------------------------------------------------------------
    #  GAME STATE
    # -----------------------------------------------------------------
    def reset_state(self):
        """Resets everything needed to start a brand-new game."""
        self.current_level = 0          # index into PRIZE_LEVELS (0-14)
        self.last_correct_amount = 0    # what the player would walk away with
        self.correct_count = 0
        self.used_question_ids = set()  # prevents repeat questions this game
        self.lifelines_used = {"fifty": False, "audience": False, "expert": False, "flip": False}

        self.current_question = None
        self.option_map = {}
        self.correct_letter = None
        self.disabled_options = set()   # letters removed by 50:50
        self.selected_letter = None
        self.answer_locked = False
        self.revealed = False
        self.hint_message = ""
        self.result_message = ""

    # -----------------------------------------------------------------
    #  START SCREEN
    # -----------------------------------------------------------------
    def create_start_screen(self):
        self.clear_container()
        frame = tk.Frame(self.container, bg=BG_MAIN)
        frame.pack(fill="both", expand=True)

        tk.Label(frame, text="THE ULTIMATE", font=FONT_SUBTITLE, fg=ACCENT_CYAN, bg=BG_MAIN).pack(pady=(70, 0))
        tk.Label(frame, text="GK CHALLENGE", font=FONT_TITLE, fg=GOLD, bg=BG_MAIN).pack(pady=(0, 10))
        tk.Label(frame, text="How far can you climb?", font=FONT_TAGLINE, fg=WHITE, bg=BG_MAIN).pack(pady=(0, 50))

        btn_frame = tk.Frame(frame, bg=BG_MAIN)
        btn_frame.pack()
        self.make_menu_button(btn_frame, "START GAME", self.start_game).pack(pady=8)
        self.make_menu_button(btn_frame, "HOW TO PLAY", self.create_how_to_play_screen).pack(pady=8)
        self.make_menu_button(btn_frame, "EXIT", self.confirm_exit).pack(pady=8)

        tk.Label(
            frame, text=f"Answer {len(PRIZE_LEVELS)} questions correctly to win Rs {format_inr(PRIZE_LEVELS[-1])}!",
            font=FONT_SMALL, fg=DIM_TEXT, bg=BG_MAIN,
        ).pack(side="bottom", pady=20)

    def create_how_to_play_screen(self):
        self.clear_container()
        frame = tk.Frame(self.container, bg=BG_MAIN)
        frame.pack(fill="both", expand=True)

        tk.Label(frame, text="HOW TO PLAY", font=FONT_SUBTITLE, fg=GOLD, bg=BG_MAIN).pack(pady=(40, 20))
        rules = (
            f"- Answer {len(PRIZE_LEVELS)} multiple-choice GK questions correctly to win "
            f"Rs {format_inr(PRIZE_LEVELS[-1])}.\n\n"
            "- Questions get progressively harder as the prize increases.\n\n"
            "- Choose your answer by clicking option A, B, C or D.\n\n"
            "- A wrong answer ends the game -- but Safe Haven levels (marked with a star) "
            "protect part of your winnings.\n\n"
            "- You can WALK AWAY at any time and keep what you've already won.\n\n"
            "LIFELINES (each usable once per game):\n"
            "   50:50 - removes two incorrect options.\n"
            "   Audience Poll - shows how the audience would vote.\n"
            "   Ask the Expert - gives you an expert's suggestion.\n"
            "   Flip the Question - swaps the current question for a new one of similar difficulty."
        )
        tk.Label(frame, text=rules, font=("Helvetica", 12), fg=WHITE, bg=BG_MAIN,
                 justify="left", wraplength=680).pack(padx=40, pady=10)
        self.make_menu_button(frame, "BACK TO MENU", self.create_start_screen).pack(pady=30)

    def confirm_exit(self):
        if messagebox.askokcancel("Exit", "Are you sure you want to exit The Ultimate GK Challenge?"):
            self.root.destroy()

    # -----------------------------------------------------------------
    #  STARTING / RESTARTING A GAME
    # -----------------------------------------------------------------
    def start_game(self):
        self.reset_state()
        self.show_question()

    def restart_game(self):
        self.reset_state()
        self.show_question()

    # -----------------------------------------------------------------
    #  QUESTION SELECTION
    # -----------------------------------------------------------------
    def get_question_for_level(self, level_index):
        """Picks a random, not-yet-used question matching this level's tier."""
        tier = LEVEL_TIER[level_index]
        pool = [q for q in QUESTION_BANK[tier] if id(q) not in self.used_question_ids]
        if not pool:  # safety net -- shouldn't normally happen
            pool = QUESTION_BANK[tier]
        question = random.choice(pool)
        self.used_question_ids.add(id(question))
        return question

    def shuffle_options(self, question):
        """Randomly places the 4 options into A/B/C/D and returns the mapping
        plus which letter is currently the correct one."""
        options_copy = question["options"][:]
        correct_text = options_copy[0]
        random.shuffle(options_copy)
        mapping = {}
        correct_letter = None
        for letter, text in zip(OPTION_LETTERS, options_copy):
            mapping[letter] = text
            if text == correct_text:
                correct_letter = letter
        return mapping, correct_letter

    def load_question(self, question):
        """Loads a question into the current game state (used for both a
        fresh question and a 'Flip the Question' swap)."""
        self.current_question = question
        self.option_map, self.correct_letter = self.shuffle_options(question)
        self.disabled_options = set()
        self.selected_letter = None
        self.answer_locked = False
        self.revealed = False
        self.hint_message = ""
        self.result_message = ""

    def show_question(self):
        """Called at the start of the game and after every correct answer."""
        question = self.get_question_for_level(self.current_level)
        self.load_question(question)
        self.render_game_screen()

    # -----------------------------------------------------------------
    #  ANSWER HANDLING
    # -----------------------------------------------------------------
    def select_answer(self, letter):
        """Called when the player clicks one of the 4 option buttons."""
        if self.answer_locked or letter in self.disabled_options:
            return
        self.selected_letter = letter
        self.answer_locked = True
        self.play_sound("select")
        self.render_game_screen()          # shows the "locked in" highlight
        self.root.after(900, self.check_answer)

    def check_answer(self):
        """Reveals whether the locked-in answer was correct or not."""
        self.revealed = True
        is_correct = (self.selected_letter == self.correct_letter)

        if is_correct:
            self.play_sound("correct")
            self.last_correct_amount = PRIZE_LEVELS[self.current_level]
            self.correct_count += 1
            self.result_message = random.choice(CORRECT_PHRASES)
        else:
            self.play_sound("wrong")
            self.result_message = "That's incorrect."

        self.render_game_screen()

        if is_correct:
            if self.current_level == len(PRIZE_LEVELS) - 1:
                self.root.after(2200, self.victory_screen)
            else:
                self.root.after(2200, self.proceed_next_question)
        else:
            self.root.after(2500, lambda: self.game_over(walked_away=False))

    def proceed_next_question(self):
        self.current_level += 1
        self.show_question()

    def compute_fallback_amount(self):
        """Amount kept after a WRONG answer -- the highest safe-haven prize
        that was already passed, or Rs 0 if none was reached yet."""
        safe_amounts = [PRIZE_LEVELS[i] for i in SAFE_HAVEN_INDICES if i < self.current_level]
        return max(safe_amounts) if safe_amounts else 0

    # -----------------------------------------------------------------
    #  LIFELINES
    # -----------------------------------------------------------------
    def use_fifty_fifty(self):
        if self.lifelines_used["fifty"] or self.answer_locked:
            return
        self.lifelines_used["fifty"] = True
        wrong_letters = [l for l in OPTION_LETTERS if l != self.correct_letter and l not in self.disabled_options]
        random.shuffle(wrong_letters)
        self.disabled_options.update(wrong_letters[:2])
        self.hint_message = "50:50 used -- two incorrect options were removed."
        self.play_sound("lifeline")
        self.render_game_screen()

    def generate_audience_poll(self):
        """Builds a percentage breakdown that usually favors the correct
        answer, without making it a dead giveaway."""
        available = [l for l in OPTION_LETTERS if l not in self.disabled_options]
        weights = {}
        for letter in available:
            if letter == self.correct_letter:
                weights[letter] = random.randint(40, 70)
            else:
                weights[letter] = random.randint(5, 30)
        total = sum(weights.values())
        percentages = {l: round(weights[l] * 100 / total) for l in available}
        diff = 100 - sum(percentages.values())
        percentages[self.correct_letter] += diff
        return percentages

    def use_audience_poll(self):
        if self.lifelines_used["audience"] or self.answer_locked:
            return
        self.lifelines_used["audience"] = True
        percentages = self.generate_audience_poll()
        parts = [f"{l}: {percentages.get(l, 0)}%" for l in OPTION_LETTERS if l not in self.disabled_options]
        self.hint_message = "Audience Poll -- " + "   ".join(parts)
        self.play_sound("lifeline")
        self.render_game_screen()

    def generate_expert_hint(self):
        """Builds an expert suggestion -- mostly right, occasionally hedgy
        or wrong, so it helps without being a guaranteed answer."""
        available = [l for l in OPTION_LETTERS if l not in self.disabled_options]
        confident_phrases = ["I'm quite confident the answer is", "I'm fairly sure it's", "I'd go with"]
        unsure_phrases = ["I'm not fully certain, but I'd lean towards",
                           "It's tricky, but my best guess is", "I think it might be"]
        if random.random() < 0.8:
            letter = self.correct_letter
            phrase = random.choice(confident_phrases)
        else:
            wrong_options = [l for l in available if l != self.correct_letter]
            letter = random.choice(wrong_options) if wrong_options else self.correct_letter
            phrase = random.choice(unsure_phrases)
        return f"{phrase} option {letter}."

    def use_expert(self):
        if self.lifelines_used["expert"] or self.answer_locked:
            return
        self.lifelines_used["expert"] = True
        self.hint_message = "Expert says: " + self.generate_expert_hint()
        self.play_sound("lifeline")
        self.render_game_screen()

    def flip_question(self):
        if self.lifelines_used["flip"] or self.answer_locked:
            return
        self.lifelines_used["flip"] = True
        new_question = self.get_question_for_level(self.current_level)
        self.load_question(new_question)
        self.hint_message = "Question flipped! Here's a new question of similar difficulty."
        self.play_sound("lifeline")
        self.render_game_screen()

    # -----------------------------------------------------------------
    #  WALK AWAY
    # -----------------------------------------------------------------
    def walk_away(self):
        if self.answer_locked:
            return
        amount = self.last_correct_amount
        if messagebox.askyesno("Walk Away", f"Walk away now and keep Rs {format_inr(amount)}?"):
            self.play_sound("walk")
            self.game_over(walked_away=True)

    # -----------------------------------------------------------------
    #  GAME SCREEN (rebuilt after every state change)
    # -----------------------------------------------------------------
    def render_game_screen(self):
        self.clear_container()

        # ---- Header ----
        header = tk.Frame(self.container, bg=BG_HEADER)
        header.pack(fill="x", side="top")
        tk.Label(header, text="THE ULTIMATE GK CHALLENGE", font=FONT_HEADER,
                 fg=GOLD, bg=BG_HEADER).pack(pady=8)

        info_bar = tk.Frame(self.container, bg=BG_MAIN)
        info_bar.pack(fill="x", padx=25, pady=(10, 5))
        tk.Label(info_bar, text=f"Prize: Rs {format_inr(PRIZE_LEVELS[self.current_level])}",
                 font=FONT_BIG_MONEY, fg=GOLD, bg=BG_MAIN).pack(side="left")
        tk.Label(info_bar, text=f"Question {self.current_level + 1}/{len(PRIZE_LEVELS)}",
                 font=FONT_HEADER, fg=WHITE, bg=BG_MAIN).pack(side="right")

        body = tk.Frame(self.container, bg=BG_MAIN)
        body.pack(fill="both", expand=True, padx=25, pady=5)

        left = tk.Frame(body, bg=BG_MAIN)
        left.pack(side="left", fill="both", expand=True)

        right = tk.Frame(body, bg=BG_PANEL, width=240)
        right.pack(side="right", fill="y", padx=(15, 0))
        right.pack_propagate(False)

        # ---- Sidebar: current winnings + prize ladder ----
        tk.Label(right, text="YOUR WINNINGS", font=FONT_SMALL, fg=ACCENT_CYAN, bg=BG_PANEL).pack(pady=(14, 2))
        tk.Label(right, text=f"Rs {format_inr(self.last_correct_amount)}",
                 font=("Helvetica", 17, "bold"), fg=GOLD, bg=BG_PANEL).pack(pady=(0, 12))
        self.build_prize_ladder(right)

        # ---- Question box ----
        q_box = tk.Frame(left, bg=BG_PANEL, bd=2, relief="ridge")
        q_box.pack(fill="x", pady=(5, 15))
        tk.Label(q_box, text=self.current_question["question"], font=FONT_QUESTION, fg=WHITE,
                 bg=BG_PANEL, wraplength=580, justify="center", padx=20, pady=20).pack()

        # ---- Answer options ----
        self.build_option_buttons(left)

        # ---- Hint / result area ----
        hint_frame = tk.Frame(left, bg=BG_MAIN)
        hint_frame.pack(fill="x", pady=(8, 0))
        if self.hint_message:
            tk.Label(hint_frame, text=self.hint_message, font=FONT_HINT, fg=ACCENT_CYAN,
                     bg=BG_MAIN, wraplength=580, justify="left").pack(anchor="w", pady=(0, 4))
        if self.revealed:
            result_color = GREEN if self.selected_letter == self.correct_letter else RED
            tk.Label(hint_frame, text=self.result_message, font=("Helvetica", 15, "bold"),
                     fg=result_color, bg=BG_MAIN).pack(anchor="w")
            tk.Label(hint_frame, text=f"Explanation: {self.current_question['explanation']}",
                     font=FONT_SMALL, fg=WHITE, bg=BG_MAIN, wraplength=580, justify="left").pack(anchor="w", pady=(2, 0))

        # ---- Lifelines ----
        self.build_lifeline_buttons(left)

        # ---- Walk away ----
        action_frame = tk.Frame(left, bg=BG_MAIN)
        action_frame.pack(fill="x", pady=(12, 5))
        walk_state = "disabled" if self.answer_locked else "normal"
        walk_btn = tk.Button(action_frame, text="WALK AWAY", font=FONT_BUTTON, bg=WALK_BG, fg=WHITE,
                              activebackground=WALK_HOVER, activeforeground=WHITE, bd=0, pady=10,
                              cursor="hand2", state=walk_state, command=self.walk_away,
                              disabledforeground=DIM_TEXT)
        walk_btn.pack(fill="x")
        if walk_state == "normal":
            self.add_hover(walk_btn, WALK_BG, WALK_HOVER)

    def build_option_buttons(self, parent):
        """Draws the four A/B/C/D answer buttons with the correct color
        for whatever state the question is currently in."""
        opts_frame = tk.Frame(parent, bg=BG_MAIN)
        opts_frame.pack(fill="x", pady=5)

        for letter in OPTION_LETTERS:
            text = self.option_map[letter]
            eliminated = letter in self.disabled_options

            if eliminated:
                display_text = f"{letter}.  (removed by 50:50)"
                bg, state = BTN_DISABLED, "disabled"
            else:
                display_text = f"{letter}.  {text}"
                if not self.answer_locked:
                    bg, state = BTN_NORMAL, "normal"
                else:
                    state = "disabled"
                    if self.revealed:
                        if letter == self.correct_letter:
                            bg = GREEN
                        elif letter == self.selected_letter:
                            bg = RED
                        else:
                            bg = BTN_DISABLED
                    else:
                        bg = BTN_SELECTED if letter == self.selected_letter else BTN_DISABLED

            btn = tk.Button(
                opts_frame, text=display_text, font=FONT_OPTION, anchor="w", justify="left",
                bg=bg, fg=WHITE, disabledforeground=WHITE, activebackground=BTN_HOVER,
                activeforeground=WHITE, bd=0, padx=15, pady=12, wraplength=540,
                state=state, cursor=("hand2" if state == "normal" else "arrow"),
                command=lambda l=letter: self.select_answer(l),
            )
            btn.pack(fill="x", pady=4)
            if state == "normal":
                self.add_hover(btn, BTN_NORMAL, BTN_HOVER)

    def build_lifeline_buttons(self, parent):
        life_frame = tk.Frame(parent, bg=BG_MAIN)
        life_frame.pack(fill="x", pady=(10, 0))

        lifeline_defs = [
            ("fifty", "50:50", self.use_fifty_fifty),
            ("audience", "AUDIENCE", self.use_audience_poll),
            ("expert", "EXPERT", self.use_expert),
            ("flip", "FLIP", self.flip_question),
        ]

        for key, label, command in lifeline_defs:
            used = self.lifelines_used[key]
            can_use = (not used) and (not self.answer_locked)
            text = label if not used else f"{label} (used)"
            bg = LIFELINE_BG if can_use else LIFELINE_USED_BG
            state = "normal" if can_use else "disabled"

            btn = tk.Button(
                life_frame, text=text, font=FONT_BUTTON, bg=bg, fg=WHITE,
                disabledforeground=DIM_TEXT, activebackground=LIFELINE_HOVER,
                activeforeground=WHITE, bd=0, pady=10, state=state,
                cursor=("hand2" if can_use else "arrow"), command=command,
            )
            btn.pack(side="left", expand=True, fill="x", padx=4)
            if can_use:
                self.add_hover(btn, LIFELINE_BG, LIFELINE_HOVER)

    def build_prize_ladder(self, parent):
        tk.Label(parent, text="PRIZE LADDER", font=FONT_SMALL, fg=ACCENT_CYAN, bg=BG_PANEL).pack(pady=(4, 6))
        ladder_frame = tk.Frame(parent, bg=BG_PANEL)
        ladder_frame.pack(fill="both", expand=True, padx=10)

        # Highest prize at the top, like a real quiz-show ladder.
        for idx in range(len(PRIZE_LEVELS) - 1, -1, -1):
            amount = PRIZE_LEVELS[idx]
            level_num = idx + 1
            is_current = (idx == self.current_level)
            is_passed = (idx < self.current_level)
            is_safe = idx in SAFE_HAVEN_INDICES

            text = f"{level_num}.  Rs {format_inr(amount)}"
            if is_safe:
                text += "  *"

            if is_current:
                fg, bg = "#1a0b3d", GOLD
            elif is_passed:
                fg, bg = GREEN, BG_PANEL
            elif is_safe:
                fg, bg = ACCENT_CYAN, BG_PANEL
            else:
                fg, bg = WHITE, BG_PANEL

            font = FONT_LADDER_CURRENT if is_current else FONT_LADDER
            tk.Label(ladder_frame, text=text, font=font, fg=fg, bg=bg,
                     anchor="w", padx=6, pady=3).pack(fill="x", pady=1)

    # -----------------------------------------------------------------
    #  GAME OVER / VICTORY SCREENS
    # -----------------------------------------------------------------
    def game_over(self, walked_away):
        self.clear_container()
        if not walked_away:
            self.play_sound("wrong")
        amount = self.last_correct_amount if walked_away else self.compute_fallback_amount()

        frame = tk.Frame(self.container, bg=BG_MAIN)
        frame.pack(fill="both", expand=True)

        title = "YOU WALKED AWAY" if walked_away else "GAME OVER"
        title_color = GOLD if walked_away else RED
        tk.Label(frame, text=title, font=("Helvetica", 34, "bold"), fg=title_color, bg=BG_MAIN).pack(pady=(60, 10))
        tk.Label(frame, text=f"You answered {self.correct_count} question(s) correctly.",
                 font=FONT_HEADER, fg=WHITE, bg=BG_MAIN).pack(pady=5)
        tk.Label(frame, text=f"You take home: Rs {format_inr(amount)}",
                 font=("Helvetica", 26, "bold"), fg=GOLD, bg=BG_MAIN).pack(pady=15)

        if not walked_away and self.current_question is not None:
            correct_text = self.current_question["options"][0]
            tk.Label(frame, text=f"Correct answer: {correct_text}", font=FONT_HINT,
                     fg=ACCENT_CYAN, bg=BG_MAIN, wraplength=650, justify="center").pack(pady=(10, 4))
            tk.Label(frame, text=self.current_question["explanation"], font=FONT_SMALL,
                     fg=WHITE, bg=BG_MAIN, wraplength=650, justify="center").pack()

        btn_frame = tk.Frame(frame, bg=BG_MAIN)
        btn_frame.pack(pady=30)
        self.make_menu_button(btn_frame, "PLAY AGAIN", self.restart_game, width=16).pack(side="left", padx=8)
        self.make_menu_button(btn_frame, "MAIN MENU", self.create_start_screen, width=16).pack(side="left", padx=8)
        self.make_menu_button(btn_frame, "EXIT", self.confirm_exit, width=16).pack(side="left", padx=8)

    def victory_screen(self):
        self.clear_container()
        self.play_sound("victory")

        frame = tk.Frame(self.container, bg="#3d2b00")
        frame.pack(fill="both", expand=True)

        tk.Label(frame, text="CONGRATULATIONS!", font=("Helvetica", 32, "bold"),
                 fg=GOLD, bg="#3d2b00").pack(pady=(70, 10))
        tk.Label(frame, text="YOU ARE THE GK CHAMPION!", font=("Helvetica", 20, "bold"),
                 fg=WHITE, bg="#3d2b00").pack(pady=10)
        tk.Label(frame, text=f"Rs {format_inr(PRIZE_LEVELS[-1])}", font=("Helvetica", 40, "bold"),
                 fg=GOLD, bg="#3d2b00").pack(pady=25)

        btn_frame = tk.Frame(frame, bg="#3d2b00")
        btn_frame.pack(pady=30)
        self.make_menu_button(btn_frame, "PLAY AGAIN", self.restart_game, width=16).pack(side="left", padx=8)
        self.make_menu_button(btn_frame, "MAIN MENU", self.create_start_screen, width=16).pack(side="left", padx=8)
        self.make_menu_button(btn_frame, "EXIT", self.confirm_exit, width=16).pack(side="left", padx=8)


# =====================================================================
#  ENTRY POINT
# =====================================================================
def main():
    root = tk.Tk()
    GKChallengeGame(root)
    root.mainloop()


if __name__ == "__main__":
    main()