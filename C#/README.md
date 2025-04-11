# TicTacToe
A game you can be an Attacker or Defender, as a user you may put an O
chess while the program might response from a X chess. Which of the
roles also gives you a chance to simulate within various cases in Debug
mode. The code enumerates a course of options, the modes are encoded in
the 2-bit field from a 32-bit type Board, and the one exceeding 2-bit is
treated as a control code to NewGame. The Startup code intends to just
reset the game without switching into other encoded mode. The Conjugate
code switches in between Attacker or Defender while the Configure code
may on or off the Debug mode when you press the key D or Escape. The
Conjugate code combining the Configure code reproduces 4 scene, which
of those can further jump in Bonus scene or Clumsy scene, where you
press the key W or L. Whenever you press the key Escape, you will
ultimately get in the original scene you held.
