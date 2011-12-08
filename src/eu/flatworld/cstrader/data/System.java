/*
   Copyright 2011 marcopar@gmail.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package eu.flatworld.cstrader.data;

public enum System {

    Adarian(251),
    Altian(189),
    Basian(153),
    Casian(211),
    Desian(253),
    Farian(213),
    Genian(230),
    Jexian(192),
    Lyrian(228),
    Nespian(150),
    Omnian(249),
    Raxian(172),
    Solian(190),
    Tyrian(168),
    Volian(232),
    Zarian(208),
    N3W3(148),
    N3W2(149),
    N3E1(151),
    N3E2(152),
    N2W2(169),
    N2W1(170),
    N2E1(171),
    N2E3(173),
    N1W3(188),
    N1E1(191),
    N1E3(193),
    S1W2(209),
    S1W1(210),
    S1E2(212),
    S2W2(229),
    S2E1(231),
    S2E3(233),
    S3W3(248),
    S3W1(250),
    S3E2(252),
    N4E3(133),
    S3E4(254),
    S4W3(268),
    N3W4(147),
    NoSystem(0);
    private final int sysid;

    System(int sysid) {
        this.sysid = sysid;
    }

    public static int stars() {
        return 16;
    }

    public static int expanses() {
        return 24;
    }

    public int getId() {
        return sysid;
    }

    public int getX() {
        return (sysid - 1) % 20;
    }

    public int getY() {
        return (sysid - 1) / 20;
    }

    public static System findSystemById(int id) {
        for (System sys : System.values()) {
            if (sys.getId() == id) {
                return sys;
            }
        }
        return null;
    }
}
