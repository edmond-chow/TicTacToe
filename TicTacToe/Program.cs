/*
 *   TicTacToe
 *   
 *   A game you can be an Attacker or Defender, as a user you may put an O
 *   chess while the program might response from a X chess. Which of the
 *   roles also gives you a chance to simulate within various cases in Debug
 *   mode. The code enumerates a course of options, the modes is encoded in
 *   the 2-bit field from value-type Board, and the one exceed 2-bit is
 *   treated as a control code to NewGame. The Startup code intends to just
 *   reset the game without switching into other encoded mode. The Conjugate
 *   code switches in between Attacker or Defender while the Configurate code
 *   may on or off the Debug mode when you press the key D or Escape. The
 *   Conjugate code combining the Configurate code reproduces 4 sence, which
 *   of those can further jump in Bonus sence or Clumsy sence, where you
 *   press the key W or L. Whenever you press the key Escape, you will
 *   ultimately get in the original scene you held.
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
using System;
using System.Windows.Forms;
namespace TicTacToe
{
    internal static class Program
    {
        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        [STAThread]
        static void Main()
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            Application.Run(new MainForm());
        }
    }
}
