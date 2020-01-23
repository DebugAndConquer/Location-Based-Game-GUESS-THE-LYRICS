-- phpMyAdmin SQL Dump
-- version 4.7.4
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Dec 06, 2019 at 06:12 AM
-- Server version: 10.1.28-MariaDB
-- PHP Version: 7.1.11

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `guess_lyrics`
--
-- --------------------------------------------------------

--
-- Table structure for table `backpack`
--
CREATE DATABASE IF NOT EXISTS `guess_lyrics`;
USE `guess_lyrics`;
CREATE TABLE `backpack` (
  `ID` int(11) NOT NULL,
  `gameMode` tinyint(4) NOT NULL,
  `SongId` int(11) NOT NULL,
  `lyricPart` varchar(150) NOT NULL,
  `numberAttempts` int(11) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `points`
--

CREATE TABLE `points` (
  `points` int(100) DEFAULT '0',
  `gameMode` tinyint(4) NOT NULL DEFAULT '0',
  `bpSize` int(11) NOT NULL DEFAULT '10'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `points`
--

INSERT INTO `points` (`points`, `gameMode`, `bpSize`) VALUES
(0, 0, 10),
(0, 1, 10);

-- --------------------------------------------------------

--
-- Table structure for table `songs`
--

CREATE TABLE `songs` (
  `id` int(11) NOT NULL,
  `gameMode` tinyint(4) NOT NULL,
  `artist` varchar(30) NOT NULL,
  `title` varchar(30) NOT NULL,
  `lyrics` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `songs`
--

INSERT INTO `songs` (`id`, `gameMode`, `artist`, `title`, `lyrics`) VALUES
(1, 0, 'ABBA', 'Happy New Year', 'abba(happy_new_year).txt'),
(2, 0, 'Mariah Carey', 'All I Want For Christmas', 'mariah_carey(all_i_want_for_christmas).txt'),
(3, 0, 'What', 'Last Christmas', 'what(last_christmas).txt'),
(4, 0, 'Bob Dylan', 'Like A Rolling Stone', 'bob_dylan(like_a_rolling_stone).txt'),
(5, 0, 'David Bowie', 'Life On Mars', 'david_bowie(life_on_mars_).txt'),
(6, 0, 'Elton John', 'Your Song', 'elton_john(your_song).txt'),
(7, 0, 'Guns n Roses', 'Sweet Child O Mine', 'guns_n_roses(sweet_child_o_mine).txt'),
(8, 0, 'John Lennon', 'Imagine', 'john_lennon(imagine).txt'),
(9, 0, 'Judy Garland', 'Over The Rainbow', 'judy_garland(over_the_rainbow).txt'),
(10, 0, 'Led Zeppelin', 'Stairway To Heaven', 'led_zeppelin(stairway_to_heaven).txt'),
(11, 0, 'Michael Jackson', 'Billie Jean', 'michael_jackson(billie_jean).txt'),
(12, 0, 'Nirvana', 'Smells Like Teen Spirit', 'nirvana(smells_like_teen_spirit).txt'),
(13, 0, 'Oasis', 'Live Forever', 'oasis(live_forever).txt'),
(14, 0, 'Queen', 'Bohemian Rhapsody', 'queen(bohemian_rhapsody).txt'),
(15, 0, 'Rolling Stones', 'I Can\'t Get No Satisfaction', 'rolling_stones(I_can\'t_get_no_satisfaction).txt'),
(16, 0, 'Sex Pistols', 'God Save The Queen', 'sex_pistols(god_save_the_queen).txt'),
(17, 0, 'The Beatles', 'Hey Jude', 'the_beatles(hey_jude).txt'),
(18, 0, 'The Clash', 'London Calling', 'the_clash(london_calling).txt'),
(19, 0, 'The Eagles', 'Hotel California', 'the_eagles(hotel_california).txt'),
(20, 0, 'The Kinks', 'Waterloo Sunset', 'the_kinks(waterloo_sunset).txt'),
(21, 0, 'U2', 'One', 'u2(one).txt'),
(22, 0, 'Whitney Houston', 'I Will Always Love You', 'whitney_houston(i_will_always_love_you).txt'),
(23, 1, 'A J Tracey', 'Ladbroke Grove', 'a_j_tracey(ladbroke_grove).txt'),
(24, 1, 'Aitch', 'Taste Make It Shake', 'aitch(taste_make_it_shake).txt'),
(25, 1, 'Ariana Grande', 'Don\'t Call Me Angel', 'ariana_grande(don\'t_call_me_angel).txt'),
(26, 1, 'Dave', 'Professor X', 'dave(professor_x).txt'),
(27, 1, 'Dermot Kennedy', 'Outnumbered', 'dermot_kennedy(outnumbered).txt'),
(28, 1, 'Dominic Fike', '3 Nights', 'dominic_fike(3_nights).txt'),
(29, 1, 'Ed Sheeran ft. Stromzy', 'Take Me Back To London', 'ed_sheeran_ft_stormzy(take_me_back_to_london).txt'),
(30, 1, 'Headie One', 'Both', 'headie_one(both).txt'),
(31, 1, 'Young T & Bugsey ft. Aitch', 'Strike A Pose', 'young_t_&_bugsey_ft_aitch(strike_a_pose).txt'),
(32, 1, 'Joel Corry', 'Sorry', 'joel_corry(sorry).txt'),
(33, 1, 'Jorja Smith ft. Burna Boy', 'Be Honest', 'jorja_smith_ft_burna_boy(be_honest).txt'),
(34, 1, 'Kygo & Whitney Houston', 'Higher Love', 'kygo_&_whitney_houston(higher_love).txt'),
(35, 1, 'Lil Tecca', 'Ransom', 'lil_tecca(ransom).txt'),
(36, 1, 'Post Malone', 'Circles', 'post_malone(circles).txt'),
(37, 1, 'Post Malone ft. Young Thug', 'Goodbyes', 'post_malone_ft_young_thug(goodbyes).txt'),
(38, 1, 'Regard', 'Ride It', 'regard(ride_it).txt'),
(39, 1, 'Sam Feldt ft. Rani', 'Post Malone', 'sam_feldt_ft_rani(post_malone).txt'),
(40, 1, 'Sam Smith', 'How Do You Sleep', 'sam_smith(how_do_you_sleep).txt'),
(41, 1, 'Shawn Mendes ft Camila Cabello', 'Senorita', 'shawn_mendes_ft_camila_cabello(senorita).txt'),
(42, 1, 'Tones & I', 'Dance Monkey', 'tones_&_i(dance_monkey).txt');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `backpack`
--
ALTER TABLE `backpack`
  ADD PRIMARY KEY (`ID`),
  ADD KEY `SongId` (`SongId`);

--
-- Indexes for table `points`
--
ALTER TABLE `points`
  ADD UNIQUE KEY `gameMode` (`gameMode`) USING BTREE;

--
-- Indexes for table `songs`
--
ALTER TABLE `songs`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `backpack`
--
ALTER TABLE `backpack`
  MODIFY `ID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=28;

--
-- AUTO_INCREMENT for table `songs`
--
ALTER TABLE `songs`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=43;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `backpack`
--
ALTER TABLE `backpack`
  ADD CONSTRAINT `backpack_ibfk_1` FOREIGN KEY (`SongId`) REFERENCES `songs` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
