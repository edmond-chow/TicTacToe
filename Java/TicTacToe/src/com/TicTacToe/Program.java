/*
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
 */
package com.TicTacToe;
public class Program {
    public static void main(String[] args) {
	    MainWindow.runLoop(new MainWindow());
    }
}
