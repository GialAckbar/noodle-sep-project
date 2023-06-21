package shared;

import client.Controller.QuizStudentFrage;
import shared.quiz.Quizfrage;

import java.util.ArrayList;
import java.util.List;



    public class FragenListe {


        List<QuizStudentFrage> list;

        public FragenListe(List<QuizStudentFrage> list) {
            this.list = list;
        }
        public FragenListe() {
            list = new ArrayList<>();
        }
        public List<QuizStudentFrage> getList() {
            return list;
        }

        public void add(QuizStudentFrage frage){
            list.add(frage);
        }

    }

