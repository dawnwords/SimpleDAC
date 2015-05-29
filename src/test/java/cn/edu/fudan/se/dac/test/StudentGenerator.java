package cn.edu.fudan.se.dac.test;

import cn.edu.fudan.se.dac.test.bean.Lecture;
import cn.edu.fudan.se.dac.test.bean.Student;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by Dawnwords on 2015/5/29.
 */
public class StudentGenerator {

    private static StudentGenerator generator = new StudentGenerator();

    public static StudentGenerator getInstance() {
        return generator;
    }

    private Random r = new Random(924);

    public List<Student> randomStudent(int number) {
        List<Student> result = new LinkedList<Student>();

        for (int i = 0; i < number; i++) {
            Student student = new Student();
            student.setId(randomId(r, 10));
            student.setName(randomName(r));
            student.setGender(r.nextBoolean());
            student.setSelectedLecture(randomLecture(r));
            result.add(student);
        }
        return result;
    }

    private List<Lecture> randomLecture(Random r) {
        List<Lecture> result = new LinkedList<Lecture>();
        for (int i = 0; i < r.nextInt(3) + 5; i++) {
            Lecture lecture = new Lecture();
            lecture.setId(randomId(r, 5));
            lecture.setName(randomName(r));
            lecture.setTeacher(randomName(r));
            result.add(lecture);
        }
        return result;
    }

    private String randomId(Random r, int len) {
        String id = "";
        for (int i = 0; i < len; i++) {
            id += String.valueOf(randomInt(r, 0, 10));
        }
        return id;
    }

    private String randomName(Random r) {
        final String[] names = {
                "Aaron", "Abbott", "Abel", "Abner", "Abraham", "Adair", "Adam", "Addison", "Adolph", "Adonis",
                "Adrian", "Ahern", "Alan", "Albert", "Aldrich", "Alexander", "Alfred", "Alger", "Algernon", "Allen",
                "Alston", "Alva", "Alvin", "Alvis", "Amos", "Andre", "Andrew", "Andy", "Angelo", "Augus",
                "Ansel", "Antony", "Antoine", "Antonio", "Archer", "Archibald", "Aries", "Arlen", "Armand", "Armstrong",
                "Arno", "Arnold", "Arthur", "Arvin", "Asa", "Ashbur", "Atwood", "Aubrey", "August", "Augustine",
                "Avery", "Baird", "Baldwin", "Bancroft", "Bard", "Barlow", "Barnett", "Baron", "Barret", "Barry",
                "Bartholomew", "Bart", "Barton", "Bartley", "Basil", "Beacher", "Beau", "Beck", "Ben", "Benedict",
                "Benjamin", "Bennett", "Benson", "Berg", "Berger", "Bernard", "Bernie", "Bert", "Berton", "Bertram",
                "Bevis", "Bill", "Bing", "Bishop", "Blair", "Blake", "Blithe", "Bob", "Booth", "Borg",
                "Boris", "Bowen", "Boyce", "Boyd", "Bradley", "Brady", "Brandon", "Brian", "Broderick", "Brook",
                "Bruce", "Bruno", "Buck", "Burgess", "Burke", "Burnell", "Burton", "Byron", "Caesar", "Calvin",
                "Carey", "Carl", "Carr", "Carter", "Cash", "Cecil", "Cedric", "Chad", "Channing", "Chapman",
                "Charles", "Chasel", "Chester", "Christ", "Christian", "Christopher", "Clare", "Clarence", "Clark", "Claude",
                "Clement", "Cleveland", "Cliff", "Clifford", "Clyde", "Colbert", "Colby", "Colin", "Conrad", "Corey",
                "Cornelius", "Cornell", "Craig", "Curitis", "Cyril", "Dana", "Daniel", "Darcy", "Darnell", "Darren",
                "Dave", "David", "Dean", "Dempsey", "Dennis", "Derrick", "Devin", "Dick", "Dominic", "Don",
                "Donahue", "Donald", "Douglas", "Drew", "Duke", "Duncan", "Dunn", "Dwight", "Dylan", "Earl",
                "Ed", "Eden", "Edgar", "Edmund", "Edison", "Edward", "Edwiin", "Egbert", "Eli", "Elijah",
                "Elliot", "Ellis", "Elmer", "Elroy", "Elton", "Elvis", "Emmanuel", "Enoch", "Eric", "Ernest",
                "Eugene", "Evan", "Everley", "Fabian", "Felix", "Ferdinand", "Fitch", "Fitzgerald", "Ford", "Francis",
                "Frank", "Franklin", "Frederic", "Gabriel", "Gale", "Gary", "Gavin", "Gene", "Geoffrey", "Geoff",
                "George", "Gerald", "Gilbert", "Giles", "Glenn", "Goddard", "Godfery", "Gordon", "Greg", "Gregary",
                "Griffith", "Grover", "Gustave", "Guy", "Hale", "Haley", "Hamiltion", "Hardy", "Harlan", "Harley",
                "Harold", "Harriet", "Harry", "Harvey", "Hayden", "Heather", "Henry", "Herbert", "Herman", "Hilary",
                "Hiram", "Hobart", "Hogan", "Horace", "Howar", "Hubery", "Hugh", "Hugo", "Humphrey", "Hunter",
                "Hyman", "Ian", "Ingemar", "Ingram", "Ira", "Isaac", "Isidore", "Ivan", "Ives", "Jack",
                "Jacob", "James", "Jared", "Jason", "Jay", "Jeff", "Jeffrey", "Jeremy", "Jerome", "Jerry",
                "Jesse", "Jim", "Jo", "John", "Jonas", "Jonathan", "Joseph", "Joshua", "Joyce", "Julian",
                "Julius", "Justin", "Keith", "Kelly", "Ken", "Kennedy", "Kenneth", "Kent", "Kerr", "Kerwin",
                "Kevin", "Kim", "King", "Kirk", "Kyle", "Lambert", "Lance", "Larry", "Lawrence", "Leif",
                "Len", "Lennon", "Leo", "Leonard", "Leopold", "Les", "Lester", "Levi", "Lewis", "Lionel",
                "Lou", "Louis", "Lucien", "Luther", "Lyle", "Lyndon", "Lynn", "Magee", "Malcolm", "Mandel",
                "Marcus", "Marico", "Mark", "Marlon", "Marsh", "Marshall", "Martin", "Marvin", "Matt", "Matthew",
                "Maurice", "Max", "Maximilian", "Maxwell", "Meredith", "Merle", "Merlin", "Michael", "Michell", "Mick",
                "Mike", "Miles", "Milo", "Monroe", "Montague", "Moore", "Morgan", "Mortimer", "Morton", "Moses",
                "Murphy", "Murray", "Myron", "Nat", "Nathan", "Nathaniel", "Neil", "Nelson", "Newman", "Nicholas",
                "Nick", "Nigel", "Noah", "Noel", "Norman", "Norton", "Ogden", "Oliver", "Omar", "Orville",
                "Osborn", "Oscar", "Osmond", "Oswald", "Otis", "Otto", "Owen", "Page", "Parker", "Paddy",
                "Patrick", "Paul", "Payne", "Perry", "Pete", "Peter", "Phil", "Philip", "Porter", "Prescott",
                "Primo", "Quentin", "Quennel", "Quincy", "Quinn", "Quintion", "Rachel", "Ralap", "Randolph", "Raymond",
                "Reg", "Regan", "Reginald", "Reuben", "Rex", "Richard", "Robert", "Robin", "Rock", "Rod",
                "Roderick", "Rodney", "Ron", "Ronald", "Rory", "Roy", "Rudolf", "Rupert", "Ryan", "Sam",
                "Sampson", "Samuel", "Sandy", "Saxon", "Scott", "Sean", "Sebastian", "Sid", "Sidney", "Silvester",
                "Simon", "Solomon", "Spencer", "Stan", "Stanford", "Stanley", "Steven", "Stev", "Steward", "Tab",
                "Taylor", "Ted", "Ternence", "Theobald", "Theodore", "Thomas", "Tiffany", "Tim", "Timothy", "Tobias",
                "Toby", "Todd", "Tom", "Tony", "Tracy", "Troy", "Truman", "Tyler", "Tyrone", "Ulysses",
                "Upton", "Uriah", "Valentine", "Valentine", "Verne", "Vic", "Victor", "Vincent", "Virgil", "Vito",
                "Vivian", "Wade", "Walker", "Walter", "Ward", "Warner", "Wayne", "Webb", "Webster", "Wendell",
                "Werner", "Wilbur", "Will", "William", "Willie", "Winfred", "Winston", "Woodrow", "Wordsworth", "Wright",
                "Wythe", "Xavier", "Yale", "Yehudi", "York", "Yves", "Zachary", "Zebulon", "Ziv"
        };
        return names[r.nextInt(names.length)];
    }

    private int randomInt(Random r, int start, int end) {
        return r.nextInt(end - start) + start;
    }
}
