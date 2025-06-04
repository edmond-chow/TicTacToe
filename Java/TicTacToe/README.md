# TicTacToe
A game you can be an Attacker or Defender, as a User you may put an O chess
while as a Response the program might put an X chess. Which of the roles also
gives you a chance to simulate within various cases in Debug mode. The code
enumerates a course of options, the Modes are encoded in the 2-bit from a 32-bit
Board, and the one exceeding 2-bit is treated as a control code to NewGame. The
Startup code intends to just reset the game without switching into other encoded
Mode. The Conjugate Side switches in between Attacker or Defender while the
Conjugate Form may on or off the Debug mode when you press the key D or Escape.
The Conjugate Side combining the Conjugate Form reproduces 4 scene, which of
those can further jump in Bonus Scene or Clumsy Scene, where you press the key W
or L. Whenever you press the key Escape, you will ultimately get in the original
scene you held.

# Enumerations
Mode
* Attacker = 0
* Defender = 1
* DebugAttacker = 2
* DebugDefender = 3
* StartupMode = 4
* SwitchSide = 5
* SwitchForm = 6
* BonusScene = 7
* ClumsyScene = 8

Turn
* Unspecified = 0
* User = 1
* Response = 2
* Terminated = 3

Result
* Empty = 0
* Won = 1
* Lost = 2
* Tied = 3

Chess
* None = 0
* X = 1
* O = 2
* Preferred = 3

Orientation
* Horizontal = 0
* Upward = 1
* Vertical = 2
* Downward = 3

# Encodings
The 32-bit Board consists of Mode encoded in a 2-bit, Turn encoded in a 2-bit,
Result encoded in a 2-bit, Round encoded in a 4-bit, State encoded in a 4-bit,
and all 9 Chesses encoded in a 2-bit. The most significant byte used to pack the
Mode, Turn, Result, and Center Chess starting from high offset bit. The 2nd most
significant byte resides the Round and State starting from high offset bit. The
least significant word holds the Circle Chesses so that Rotate is handy to
implement while left shifting every 2 bits and inserting its leftmost overflows
means counter-clockwise a 45-degree. We might access some field through Offset
and Mask from the indices.

An array listing all the offsets for the fields
* Offset = { 20, 12, 10, 8, 14, 24, 6, 0, 2, 4, 30, 28, 26, 16 }

The indices used to address the actual field from Offset and Mask
* IRound = 0
* IMode = 10
* ITurn = 11
* IResult = 12
* IState = 13
* The Chesses on Board denoted from 1 through 9

Special Masks for Validation in 32-bit with Boards
* First3 = 0x3F00
* Circle = 0xFFFF
* Center = 0xFF00

The Chesses on Board may be Rotate to any Orientation or Reflect with an axis on
an Orientation. The operations of Parse1 for 45-degree, Parse2 for right angle,
and Parse4 for flat angle on or off can be exchanged in any order. The trick how
the operations of Parse8 on or off also can work with that set of operations on
the same means is the reflection axis to Reflect follows the displacement of the
total angles from other Parses. Board.ParseState lists out all the combination
you would like while Board.LocateChess locates where the prefer Chesses reside.
The Case gets you accessing a 32-bit to denote all 9 Chesses with encoded datum.

The offsets packing encoded datum showing a Case (Starting from high offset bit)
* The Chesses 1 through 3 on Board reside the bits 16 through 21
* The Chesses 4 through 6 on Board reside the bits 8 through 13
* The Chesses 7 through 9 on Board reside the bits 0 through 5
* The rest are reserved (shall be zeros)

The Pack receive a 32-bit Source to create Boards, the 24th through 27th least
significant bits storing a State for listing various Boards through
Board.ParseState while the least significant 3 bytes same as packing encoded
datum showing a Case and the rest are reserved that shall be zeros.

The Tuple receive a 32-bit Code to create a course of Packs (Won, Lost and Mask)
where the bits interpretation of Code same as Source. The original bits storing
9 Chesses now are for control codes to generate those 3 Packs.

Won Pack Transformation
* Empty (0b00) -> Chess.None (0b00)
* Except (0b01) -> Chess.None (0b00)
* Preserve (0b10) -> Chess.O (0b10)
* Preferred (0b11) -> Chess.Preferred (0b11)

Lost Pack Transformation
* Empty (0b00) -> Chess.None (0b00)
* Except (0b01) -> Chess.None (0b00)
* Preserve (0b10) -> Chess.X (0b01)
* Preferred (0b11) -> Chess.Preferred (0b11)

Mask Pack Transformation
* Empty (0b00) -> Chess.Preferred (0b11)
* Except (0b01) -> Chess.None (0b00)
* Preserve (0b10) -> Chess.Preferred (0b11)
* Preferred (0b11) -> Chess.Preferred (0b11)

Special Masks for Validation in 32-bit with Sources and Codes
* Field = 0xF3F3F3F
* Parse = 0xF000000
* Match = 0x3F3F3F

The Tuple might use the helper Boxes to transfer them one by one.
