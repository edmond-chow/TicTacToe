"""
 *   TicTacToe
 *
 *   A game you can be an Attacker or Defender, as a User you may put an O
 *   chess while as a Response the program might put an X chess. Which of the
 *   roles also gives you a chance to simulate within various cases in Debug
 *   mode. The code enumerates a course of options, the Modes are encoded in
 *   the 2-bit from a 32-bit Board, and the one exceeding 2-bit is treated as
 *   a control code to NewGame. The Startup code intends to just reset the
 *   game without switching into other encoded Mode. The Conjugate Side
 *   switches in between Attacker or Defender while the Conjugate Form may on
 *   or off the Debug mode when you press the key D or Escape. The Conjugate
 *   Side combining the Conjugate Form reproduces 4 scene, which of those can
 *   further jump in Bonus Scene or Clumsy Scene, where you press the key W
 *   or L. Whenever you press the key Escape, you will ultimately get in the
 *   original scene you held.
 *
 *   Copyright (C) 2025  Edmond Chow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *   If you have any inquiry, feel free to contact <edmond-chow@outlook.com>.
"""
from copy import copy
from enum import Enum
import random
from typing import Final
from PySide6.QtCore import Qt
from PySide6.QtGui import QIcon, QFont, QMouseEvent, QKeyEvent, QShowEvent
from PySide6.QtWidgets import QMainWindow, QPushButton
field: Final[int] = 0xF3F3F3F
parse: Final[int] = 0xF000000
match: Final[int] = 0x3F3F3F
class Mode(Enum):
    Attacker = 0
    Defender = 1
    DebugAttacker = 2
    DebugDefender = 3
    StartupMode = 4
    SwitchSide = 5
    SwitchForm = 6
    BonusScene = 7
    ClumsyScene = 8
    def __int__(self):
        return self.value
class Turn(Enum):
    Unspecified = 0
    User = 1
    Response = 2
    Terminated = 3
    def __int__(self):
        return self.value
class Result(Enum):
    Empty = 0
    Won = 1
    Lost = 2
    Tied = 3
    def __int__(self):
        return self.value
class Chess(Enum):
    Empty = 0
    X = 1
    O = 2
    Preferred = 3
    def __int__(self):
        return self.value
class Orientation(Enum):
    Horizontal = 0
    Upward = 1
    Vertical = 2
    Downward = 3
    def __int__(self):
        return self.value
first3: Final[int] = 0x3F00
circle: Final[int] = 0xFFFF
center: Final[int] = 0xFF00
box: Final[int] = 0b11
side: Final[int] = 0b1
form: Final[int] = 0b10
p1: Final[int] = 0b1
p2: Final[int] = 0b10
p4: Final[int] = 0b100
p8: Final[int] = 0b1000
i_round: Final[int] = 0
i_mode: Final[int] = 10
i_turn: Final[int] = 11
i_result: Final[int] = 12
i_state: Final[int] = 13
d_offset: Final[list[int]] = [20, 12, 10, 8, 14, 24, 6, 0, 2, 4, 30, 28, 26, 16]
def __initialize_mask__():
    rst = [0] * len(d_offset)
    rst[i_round] = 0xF00000
    rst[i_state] = 0xF0000
    for i in range(1, len(rst) - 1):
        rst[i] = box << d_offset[i]
    return rst
d_mask: Final[list[int]] = __initialize_mask__()
class Board:
    def __getitem__(self, i: int):
        if type(i) is not int:
            raise TypeError(i)
        if i < 1 or i > 9:
            raise IndexError()
        return Chess((self.data & d_mask[i]) >> d_offset[i])
    def __setitem__(self, i: int, value: Chess):
        if type(i) is not int:
            raise TypeError(i)
        if i < 1 or i > 9:
            raise IndexError()
        self.data &= ~d_mask[i]
        self.data |= (int(value) << d_offset[i]) & d_mask[i]
    @property
    def mode(self):
        return Mode((self.data & d_mask[i_mode]) >> d_offset[i_mode])
    @mode.setter
    def mode(self, value: Mode):
        self.data &= ~d_mask[i_mode]
        self.data |= (int(value) << d_offset[i_mode]) & d_mask[i_mode]
    @property
    def conjugate_side(self):
        return Mode((int(self.mode) & form) | (~int(self.mode) & side))
    @property
    def conjugate_form(self):
        return Mode((int(self.mode) & side) | (~int(self.mode) & form))
    @property
    def on_defender_side(self):
        return (int(self.mode) & side) == side
    @property
    def in_debug_form(self):
        return (int(self.mode) & form) == form
    @property
    def turn(self):
        return Turn((self.data & d_mask[i_turn]) >> d_offset[i_turn])
    @turn.setter
    def turn(self, value: Turn):
        self.data &= ~d_mask[i_turn]
        self.data |= (int(value) << d_offset[i_turn]) & d_mask[i_turn]
    @property
    def result(self):
        return Result((self.data & d_mask[i_result]) >> d_offset[i_result])
    @result.setter
    def result(self, value: Result):
        self.data &= ~d_mask[i_result]
        self.data |= (int(value) << d_offset[i_result]) & d_mask[i_result]
    @property
    def round(self):
        return (self.data & d_mask[i_round]) >> d_offset[i_round]
    @round.setter
    def round(self, value: int):
        self.data &= ~d_mask[i_round]
        self.data |= (int(value) << d_offset[i_round]) & d_mask[i_round]
    @property
    def state(self):
        return (self.data & d_mask[i_state]) >> d_offset[i_state]
    @state.setter
    def state(self, value: int):
        self.data &= ~d_mask[i_state]
        self.data |= (int(value) << d_offset[i_state]) & d_mask[i_state]
    @property
    def moves(self):
        return self.state & 0b111
    @property
    def orient(self):
        return Orientation(self.state & box)
    @property
    def parse1(self):
        return (self.state & p1) == p1
    @parse1.setter
    def parse1(self, value: bool):
        if type(value) is not bool:
            raise TypeError(value)
        origin = (self.state & p1) == p1
        if origin == value:
            return
        if value:
            self.rotate(1)
            self.state |= p1
        else:
            self.rotate(-1)
            self.state &= ~p1
    @property
    def parse2(self):
        return (self.state & p2) == p2
    @parse2.setter
    def parse2(self, value: bool):
        if type(value) is not bool:
            raise TypeError(value)
        origin = (self.state & p2) == p2
        if origin == value:
            return
        if value:
            self.rotate(2)
            self.state |= p2
        else:
            self.rotate(-2)
            self.state &= ~p2
    @property
    def parse4(self):
        return (self.state & p4) == p4
    @parse4.setter
    def parse4(self, value: bool):
        if type(value) is not bool:
            raise TypeError(value)
        origin = (self.state & p4) == p4
        if origin == value:
            return
        if value:
            self.rotate(4)
            self.state |= p4
        else:
            self.rotate(-4)
            self.state &= ~p4
    @property
    def parse8(self):
        return (self.state & p8) == p8
    @parse8.setter
    def parse8(self, value: bool):
        if type(value) is not bool:
            raise TypeError(value)
        origin = (self.state & p8) == p8
        if origin == value:
            return
        self.reflect(self.orient)
        if value:
            self.state |= p8
        else:
            self.state &= ~p8
    @property
    def case(self):
        rst = (self.data & first3) >> 6
        for i in range(4, 10):
            rst <<= 4 if i == 7 else 2
            rst |= (self.data & d_mask[i]) >> d_offset[i]
        return rst
    @case.setter
    def case(self, value: int):
        if type(value) is not int:
            raise TypeError(value)
        self.data &= ~first3
        self.data |= (value >> 8) & first3
        rest = value
        for i in range(9, 3, -1):
            self.data &= ~d_mask[i]
            self.data |= (rest & box) << d_offset[i]
            rest >>= 4 if i == 7 else 2
    @property
    def sanitizer(self):
        rst = copy(self)
        for i in range(1, 10):
            if rst[i] == Chess.Preferred:
                rst[i] = Chess.Empty
        return rst
    def __init__(self, mode):
        if type(mode) is Mode:
            self.data = 0
            if (int(mode) & side) == 0b0:
                self.turn = Turn.User
            else:
                self.turn = Turn.Response
            self.mode = mode
        elif type(mode) is int:
            self.data = 0
            self.case = mode
        else:
            raise TypeError(mode)
    def locate_chess(self, chess: Chess):
        if type(chess) is not Chess:
            raise TypeError(chess)
        rst = list()
        for i in range(1, 10):
            if self[i] == chess:
                rst.append(i)
        return rst
    def parse_state(self, state: int):
        if type(state) is not int:
            raise TypeError(state)
        c1 = (state & p1) == p1
        c2 = (state & p2) == p2
        c4 = (state & p4) == p4
        c8 = (state & p8) == p8
        v1 = self.parse1
        v2 = self.parse2
        v4 = self.parse4
        v8 = self.parse8
        sz = 1
        if c1:
            sz *= 2
        if c2:
            sz *= 2
        if c4:
            sz *= 2
        if c8:
            sz *= 2
        rst = [self] * sz
        for i in range(0, sz):
            rst[i] = copy(self)
        dx = 1
        if c1:
            dy = dx
            dx *= 2
            for i in range(dy, sz):
                if i % dx == 0:
                    i += dy
                rst[i].parse1 = not v1
        if c2:
            dy = dx
            dx *= 2
            for i in range(dy, sz):
                if i % dx == 0:
                    i += dy
                rst[i].parse2 = not v2
        if c4:
            dy = dx
            dx *= 2
            for i in range(dy, sz):
                if i % dx == 0:
                    i += dy
                rst[i].parse4 = not v4
        if c8:
            dy = dx
            dx *= 2
            for i in range(dy, sz):
                if i % dx == 0:
                    i += dy
                rst[i].parse8 = not v8
        return rst
    def __str__(self):
        rst = "Board < "
        rst += str(self.mode)
        rst += ", "
        rst += str(self.turn)
        rst += ", "
        rst += str(self.result)
        rst += " > { 0x"
        rst += '{:X}'.format(self.round)
        rst += " } [ "
        for i in range(1, 10):
            if self[i] == Chess.Empty:
                rst += "_"
            elif self[i] == Chess.X:
                rst += "X"
            elif self[i] == Chess.O:
                rst += "O"
            elif self[i] == Chess.Preferred:
                rst += "+"
            if i == 3 or i == 6:
                rst += ", "
        rst += " ] ( 0b"
        rst += '{:04b}'.format(self.state)
        rst += ", "
        rst += "↓" if self.parse8 else "↑"
        rst += str(self.moves * 45)
        rst += "°, "
        rst += str(self.orient)
        rst += " )"
        return rst
    def rotate(self, moves: int):
        if type(moves) is not int:
            raise TypeError(moves)
        moves %= 8
        if moves < 0:
            moves += 8
        nears = self.data & circle
        nears <<= moves * 2
        nears |= nears >> 16
        self.data &= ~circle
        self.data |= nears & circle
    def reflect(self, orient: Orientation):
        if type(orient) is not Orientation:
            raise TypeError(orient)
        if orient == Orientation.Horizontal:
            lines = self.case
            fst3 = lines >> 16
            lst3 = lines << 16
            lines &= center
            lines |= fst3
            lines |= lst3
            self.case = lines
        elif orient == Orientation.Upward:
            self.rotate(-1)
            self.reflect(Orientation.Horizontal)
            self.rotate(1)
        elif orient == Orientation.Vertical:
            self.rotate(-2)
            self.reflect(Orientation.Horizontal)
            self.rotate(2)
        elif orient == Orientation.Downward:
            self.rotate(-3)
            self.reflect(Orientation.Horizontal)
            self.rotate(3)
    def clear_parse(self):
        self.rotate(-self.moves)
        if self.parse8:
            self.reflect(Orientation.Horizontal)
        self.state = 0
    def reset(self):
        self.data = 0
class Pack:
    @property
    def boards(self):
        rst = copy(self.parses)
        for i in range(0, len(rst)):
            rst[i] = copy(rst[i])
        return rst
    @property
    def source(self):
        return self.data
    def __init__(self, source: int):
        if type(source) is not int:
            raise TypeError(source)
        self.data = source & field
        self.refer = Board(self.data)
        self.parses = self.refer.parse_state(self.data >> 24)
    def __str__(self):
        rst = "Pack [ "
        for i in range(1, 10):
            if self.refer[i] == Chess.Empty:
                rst += "_"
            elif self.refer[i] == Chess.X:
                rst += "X"
            elif self.refer[i] == Chess.O:
                rst += "O"
            elif self.refer[i] == Chess.Preferred:
                rst += "+"
            if i == 3 or i == 6:
                rst += ", "
        rst += " ] ( 0b"
        rst += '{:04b}'.format(self.data >> 24)
        rst += " )"
        return rst
class Boxes:
    def __getitem__(self, i: int):
        i %= 16
        if i < 0:
            i += 16
        return (self.data >> (i * 2)) & box
    def __setitem__(self, i: int, value: int):
        i %= 16
        if i < 0:
            i += 16
        self.data &= ~(box << (i * 2))
        self.data |= (value & box) << (i * 2)
    @property
    def values(self):
        return self.data
    def __init__(self, values: int):
        self.data = values
class Tuple:
    def __init__(self, code: int):
        self.data = code & field
        b_won = Boxes(code)
        b_lost = Boxes(code)
        b_mask = Boxes(code)
        for i in range(0, 11):
            if b_won[i] == 0b01:
                b_won[i] = 0b00
            if b_lost[i] == 0b01:
                b_lost[i] = 0b00
            elif b_lost[i] == 0b10:
                b_lost[i] = 0b01
            if b_mask[i] == 0b01:
                b_mask[i] = 0b00
            else:
                b_mask[i] = 0b11
        self.won = Pack((b_won.values & match) | (code & parse))
        self.lost = Pack((b_lost.values & match) | (code & parse))
        self.mask = Pack((b_mask.values & match) | (code & parse))
    def __str__(self):
        b_data = Boxes(self.data)
        rst = "Tuple [ "
        for i in range(10, -1, -1):
            if i == 3 or i == 7:
                rst += ", "
            elif b_data[i] == 0b00:
                rst += "_"
            elif b_data[i] == 0b01:
                rst += "~"
            elif b_data[i] == 0b10:
                rst += "$"
            elif b_data[i] == 0b11:
                rst += "+"
        rst += " ] ( 0b"
        rst += '{:04b}'.format(self.data >> 24)
        rst += " )"
        return rst
zero_survive: Final[list[Tuple]] = [
    Tuple(0b0011_00011001_00011001_00011001),
    Tuple(0b0110_00100101_00100101_00100101),
]
single_survive: Final[list[Tuple]] = [
    Tuple(0b0011_00011001_00011101_00011001),
    Tuple(0b0111_00011001_00011001_00011101),
    Tuple(0b0110_00100101_00110101_00100101),
    Tuple(0b1110_00100101_00100101_00110101),
]
double_survive: Final[list[Tuple]] = [
    Tuple(0b1110_00010111_00011010_00000100),
    Tuple(0b1110_00010111_00011000_00000110),
    Tuple(0b1110_00010111_00010010_00100100),
    Tuple(0b1110_00010111_00010000_00100110),
    Tuple(0b0110_00001011_00010110_00010100),
    Tuple(0b0110_00100011_00010110_00010100),
    Tuple(0b0110_00001011_00010100_00010110),
    Tuple(0b0110_00100011_00010100_00010110),
]
cases: Final[list[Pack]] = [
    Pack(0b0000_00110011_00001100_00110011),
    Pack(0b0000_00110011_00001000_00110011),
    Pack(0b0111_00001000_00001100_00000000),
]
mask_a: Final[Pack] = Pack(0b1111_00111111_00111111_00111111)
class ChessButton(QPushButton):
    def __init__(self, window: QMainWindow):
        super().__init__(window)
        font = QFont()
        font.setFamily("Consolas")
        font.setPointSizeF(24)
        self.setFont(font)
    def mousePressEvent(self, e: QMouseEvent):
        self.window().button_chess_click(self, e)
    def keyPressEvent(self, e: QKeyEvent):
        self.window().button_chess_keydown(self, e)
class SwitchButton(QPushButton):
    def __init__(self, window: QMainWindow):
        super().__init__(window)
        font = QFont()
        font.setFamily("Microsoft JhengHei")
        font.setPointSizeF(9)
        self.setFont(font)
        self.setText("Switch")
    def mousePressEvent(self, e: QMouseEvent):
        self.window().button_switch_click(self, e)
    def keyPressEvent(self, e: QKeyEvent):
        self.window().button_chess_keydown(self, e)
class ResetButton(QPushButton):
    def __init__(self, window: QMainWindow):
        super().__init__(window)
        font = QFont()
        font.setFamily("Microsoft JhengHei")
        font.setPointSizeF(9)
        self.setFont(font)
        self.setText("Reset")
    def mousePressEvent(self, e: QMouseEvent):
        self.window().button_reset_click(self, e)
    def keyPressEvent(self, e: QKeyEvent):
        self.window().button_chess_keydown(self, e)
class MainWindow(QMainWindow):
    @property
    def title(self):
        rst = "< Debug > " if self.bo.in_debug_form else ""
        if self.lst_mo != Mode.StartupMode:
            rst = "< Clumsy > " if self.bo.on_defender_side else "< Bonus > "
        rst += "TicTacToe"
        rst += " Defender" if self.bo.on_defender_side else " Attacker"
        if self.bo.result == Result.Won:
            rst += " [ Win ]"
        elif self.bo.result == Result.Lost:
            rst += " [ Lost ]"
        elif self.bo.result == Result.Tied:
            rst += " [ Tied ]"
        return rst
    @property
    def mo(self):
        return self.bo.mode
    @mo.setter
    def mo(self, value: Mode):
        if self.bo.mode == value:
            return
        self.bo = Board(value)
        self.setWindowTitle(self.title)
    @property
    def tu(self):
        return self.bo.turn
    @tu.setter
    def tu(self, value: Turn):
        if value == Turn.Unspecified:
            self.bo = Board(self.bo.mode)
        elif value == Turn.Terminated or self.bo.turn == Turn.Terminated:
            self.bo.turn = Turn.Terminated
            self.bo.round = 9
        elif self.bo.turn != value and self.bo.round < 9:
            self.bo.turn = value
            self.bo.round = self.bo.round + 1
        self.setWindowTitle(self.title)
    @property
    def re(self):
        return self.bo.result
    @re.setter
    def re(self, value: Result):
        if self.bo.result == value:
            return
        if value == Result.Empty:
            self.bo = Board(self.bo.mode)
        else:
            self.bo.turn = Turn.Terminated
            self.bo.round = 9
        self.bo.result = value
        self.setWindowTitle(self.title)
    def initialize_component(self):
        self.button1 = ChessButton(self)
        self.button2 = ChessButton(self)
        self.button3 = ChessButton(self)
        self.button4 = ChessButton(self)
        self.button5 = ChessButton(self)
        self.button6 = ChessButton(self)
        self.button7 = ChessButton(self)
        self.button8 = ChessButton(self)
        self.button9 = ChessButton(self)
        self.button_switch = SwitchButton(self)
        self.button_reset = ResetButton(self)
        self.button1.setGeometry(12, 12, 80, 80)
        self.button2.setGeometry(98, 12, 80, 80)
        self.button3.setGeometry(184, 12, 80, 80)
        self.button4.setGeometry(12, 98, 80, 80)
        self.button5.setGeometry(98, 98, 80, 80)
        self.button6.setGeometry(184, 98, 80, 80)
        self.button7.setGeometry(12, 184, 80, 80)
        self.button8.setGeometry(98, 184, 80, 80)
        self.button9.setGeometry(184, 184, 80, 80)
        self.button_switch.setGeometry(12, 270, 123, 35)
        self.button_reset.setGeometry(141, 270, 123, 35)
        self.setFixedSize(276, 317)
        self.setWindowTitle("TicTacToe")
        self.setWindowIcon(QIcon(":icons/TicTacToe.ico"))
        self.setWindowFlags(Qt.WindowType.Window | Qt.WindowType.WindowTitleHint | Qt.WindowType.WindowCloseButtonHint)
    def showEvent(self, e: QShowEvent):
        self.main_window_load(self, e)
    def __init__(self):
        super().__init__(None)
        self.button1 = None
        self.button2 = None
        self.button3 = None
        self.button4 = None
        self.button5 = None
        self.button6 = None
        self.button7 = None
        self.button8 = None
        self.button9 = None
        self.button_switch = None
        self.button_reset = None
        self.initialize_component()
        self.lst_mo = Mode.StartupMode
        self.bo = Board(Mode.Attacker)
        self.co = [self, self.button1, self.button2, self.button3, self.button4, self.button5, self.button6, self.button7, self.button8, self.button9, self.button_switch, self.button_reset]
    @staticmethod
    def run_loop(window):
        window.show()
    def choose_chess(self, chosen: list[int]):
        self.put_chess(self.co[chosen[random.randint(0, len(chosen) - 1)]])
    def process_response(self, case: list[Board], mask: list[Board]):
        for i in range(0, len(case)):
            if (self.bo.case & mask[i].case) == case[i].sanitizer.case:
                self.choose_chess(case[i].locate_chess(Chess.Preferred))
                return True
        return False
    def check_response(self):
        for p in cases:
            if self.process_response(p.boards, mask_a.boards):
                return
        for t in single_survive:
            if self.process_response(t.lost.boards, t.mask.boards):
                return
        for t in single_survive:
            if self.process_response(t.won.boards, t.mask.boards):
                return
        for t in double_survive:
            if self.process_response(t.lost.boards, t.mask.boards):
                return
        for t in double_survive:
            if self.process_response(t.won.boards, t.mask.boards):
                return
        self.choose_chess(self.bo.locate_chess(Chess.Empty))
    def process_result(self, case: list[Board], mask: list[Board], result: Result):
        for i in range(0, len(case)):
            if (self.bo.case & mask[i].case) == case[i].case:
                self.re = result
                return True
        return False
    def check_result(self):
        if self.re != Result.Empty:
            return
        for t in zero_survive:
            if self.process_result(t.lost.boards, t.mask.boards, Result.Lost):
                return
        for t in zero_survive:
            if self.process_result(t.won.boards, t.mask.boards, Result.Won):
                return
        if self.bo.round == 9:
            self.re = Result.Tied
    def new_game(self, mode: Mode):
        if mode == Mode.StartupMode or self.mo == mode:
            self.tu = Turn.Unspecified
        elif mode == Mode.SwitchSide:
            self.mo = self.bo.conjugate_side
        elif mode == Mode.SwitchForm:
            self.mo = self.bo.conjugate_form
        elif mode == Mode.BonusScene:
            if self.lst_mo == Mode.StartupMode:
                self.lst_mo = self.mo
            if self.mo == Mode.DebugAttacker:
                self.tu = Turn.Unspecified
            else:
                self.mo = Mode.DebugAttacker
        elif mode == Mode.ClumsyScene:
            if self.lst_mo == Mode.StartupMode:
                self.lst_mo = self.mo
            if self.mo == Mode.DebugDefender:
                self.tu = Turn.Unspecified
            else:
                self.mo = Mode.DebugDefender
        else:
            self.mo = mode
        self.button_reset.setDisabled(True)
        for i in range(1, 10):
            self.co[i].setText("")
            self.co[i].setStyleSheet("color: black")
        if self.lst_mo != Mode.StartupMode:
            self.put_chess(self.button1)
            self.put_chess(self.button2)
            self.put_chess(self.button3)
            self.put_chess(self.button6)
            self.put_chess(self.button9)
            self.put_chess(self.button8)
            self.put_chess(self.button7)
            self.put_chess(self.button4)
            self.put_chess(self.button5)
        elif self.bo.on_defender_side and not self.bo.in_debug_form:
            self.check_response()
    def put_chess(self, target: QPushButton):
        self.button_reset.setDisabled(False)
        i = self.co.index(target)
        if self.bo[i] == Chess.Empty and self.re == Result.Empty:
            if self.tu == Turn.User:
                self.bo[i] = Chess.O
                target.setText(" O ")
                target.setStyleSheet("color: green")
                self.tu = Turn.Response
                self.check_result()
                if not self.bo.in_debug_form and self.re == Result.Empty:
                    self.check_response()
                    self.check_result()
            elif self.tu == Turn.Response:
                self.bo[i] = Chess.X
                target.setText(" X ")
                target.setStyleSheet("color: red")
                self.tu = Turn.User
                self.check_result()
    def button_switch_click(self, sender: QPushButton, e: QMouseEvent):
        if self.lst_mo != Mode.StartupMode:
            self.new_game(Mode.BonusScene if self.bo.on_defender_side else Mode.ClumsyScene)
        else:
            self.new_game(Mode.SwitchSide)
    def button_reset_click(self, sender: QPushButton, e: QMouseEvent):
        if self.lst_mo != Mode.StartupMode:
            mo = self.lst_mo
            self.lst_mo = Mode.StartupMode
            self.new_game(mo)
        else:
            self.new_game(Mode.StartupMode)
    def main_window_load(self, sender: QMainWindow, e: QShowEvent):
        self.new_game(Mode.StartupMode)
    def button_chess_keydown(self, sender: QPushButton, e: QKeyEvent):
        if e.key() == Qt.Key.Key_W:
            self.new_game(Mode.BonusScene)
        elif e.key() == Qt.Key.Key_L:
            self.new_game(Mode.ClumsyScene)
        elif e.key() == Qt.Key.Key_Escape and self.lst_mo != Mode.StartupMode:
            mo = self.lst_mo
            self.lst_mo = Mode.StartupMode
            self.new_game(mo)
        elif e.key() == Qt.Key.Key_D and not self.bo.in_debug_form:
            self.new_game(self.bo.conjugate_form)
        elif e.key() == Qt.Key.Key_Escape and self.bo.in_debug_form:
            self.new_game(self.bo.conjugate_form)
    def button_chess_click(self, sender: QPushButton, e: QMouseEvent):
        self.put_chess(sender)
