package org.mario.btle_1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TortyActivity extends AppCompatActivity {

    public void irMain(){
        Intent intent = new Intent(this, MainActivity.class);
        // Hola Tortosa, ¡recuerda responder con tu famosa frase! (tota pedra...)
        // AQUI TU RESPUESTA:
        startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_torty);
        Button homeButton = findViewById(R.id.button_home_torty);
        homeButton.setOnClickListener(v->irMain());
        TextView textView = findViewById(R.id.textView);
        // Puedes establecer el texto dinámicamente aquí
        textView.setText("INFORMACION ADICIONAL SOBRE GASES CONTAMINANTES\n" +
                "OZONO(O3)\n" +
                "Causas\n" +
                "Contaminantes precursorios\n" +
                "Los principales precursores del ozono troposférico son los óxidos de nitrógeno (NOx) y los compuestos orgánicos volátiles (COV). Estos se emiten principalmente por actividades humanas como la quema de combustibles fósiles, la industria y las emisiones vehiculares.\n" +
                "\n" +
                "Radiacion solar\n" +
                "La formación de ozono troposférico requiere la presencia de radiación solar para activar los precursores. Por lo tanto, la concentración de ozono tiende a ser más alta en áreas con mucha luz solar.\n" +
                "\n" +
                "Efectos\n" +
                "Salud humana\n" +
                "La exposición a niveles elevados de ozono puede causar problemas respiratorios, exacerbación de enfermedades respiratorias preexistentes como el asma, irritación de los ojos y la garganta, y disminución de la función pulmonar.\n" +
                "\n" +
                "Vegetacion\n" +
                "El ozono también puede dañar los tejidos de las plantas, afectando su crecimiento y rendimiento.\n" +
                "\n" +
                "Valores limite\n" +
                "Los valores límite de ozono varían según las normativas establecidas por diferentes organismos. La Organización Mundial de la Salud (OMS) recomienda un valor medio diario de 100 microgramos por metro cúbico (µg/m³) como límite para la protección de la salud humana.\n" +
                "\n" +
                "Consejos para respirar un aire mas limpio\n" +
                "Monitoreo de la calidad del aire a traves de aplicaciones o sitios web que proporcionen informacion en tiempo real sobre los niveles\n" +
                "Evitar actividades al aire libre en momentos criticos evitando actividades las horas pico generalmente en fias calidos y soleados\n" +
                "Usar trasporte sostenible para reducir las emisiones de vehiculos eligiendo formas de transporte mas sostenible\n" +
                "DIOXIDO DE CARBONO(CO2)\n" +
                "Causas\n" +
                "Combustion de combustibles fosiles\n" +
                "La principal fuente antropogénica de dióxido de carbono es la quema de carbón, petróleo y gas natural para la generación de energía y el transporte.\n" +
                "\n" +
                "Deforestacion\n" +
                "La eliminación de bosques reduce la capacidad de absorción de CO2, contribuyendo al aumento de los niveles atmosféricos.\n" +
                "\n" +
                "Procesos industriales\n" +
                "Algunas actividades industriales liberan CO2 como subproducto.\n" +
                "\n" +
                "Efectos\n" +
                "Cambio climatico\n" +
                "El CO2 es uno de los principales gases de efecto invernadero, contribuyendo al calentamiento global y al cambio climático.\n" +
                "\n" +
                "Acidificacion del oceano\n" +
                "La absorción de CO2 por los océanos resulta en la acidificación del agua, lo que puede afectar negativamente a los organismos marinos.\n" +
                "\n" +
                "Valores limite\n" +
                "A diferencia de muchos contaminantes atmosféricos, el CO2 no tiene valores límite específicos en las regulaciones de calidad del aire porque no se considera un contaminante directo para la salud. Sin embargo, existen límites y objetivos a nivel global para la reducción de emisiones en acuerdos como el Acuerdo de París.\n" +
                "\n" +
                "Consejos para respirar un aire mas limpio\n" +
                "Opta por formas de transporte mas sostenibles, ya sea tanto ir en transporte publico, como ir en bicicleta\n" +
                "Apoya la conservación de bosques y otras áreas verdes, ya que actúan como sumideros naturales de CO2.\n" +
                "DIOXIDO DE AZUFRE(SO2)\n" +
                "Causas\n" +
                "Quema de combustibles fosiles\n" +
                "La principal fuente antropogénica de dióxido de azufre es la quema de carbón y petróleo en centrales eléctricas y procesos industriales.\n" +
                "\n" +
                "Refinacion de petroleo\n" +
                "La refinería de petróleo es otra fuente importante de emisiones de SO2.\n" +
                "\n" +
                "Procesos industriales\n" +
                "Algunas industrias emiten dióxido de azufre como resultado de ciertos procesos químicos.\n" +
                "\n" +
                "Efectos\n" +
                "Problemas respiratorios\n" +
                "La exposición a altos niveles de SO2 puede causar problemas respiratorios, especialmente en personas con afecciones respiratorias preexistentes como el asma.\n" +
                "\n" +
                "Impacto ambiental\n" +
                "El SO2 puede contribuir a la lluvia ácida, que tiene efectos perjudiciales en los suelos, cuerpos de agua y la vegetación.\n" +
                "\n" +
                "Daño a materiales\n" +
                "El dióxido de azufre puede dañar edificios, monumentos y otros materiales debido a la formación de ácido sulfúrico.\n" +
                "\n" +
                "Valores limite\n" +
                "En la Unión Europea, el valor límite para la protección de la salud humana es de 125 microgramos por metro cúbico (µg/m³) como media móvil diaria, y de 350 µg/m³ como media horaria, no debiendo superarse más de 24 veces al año.\n" +
                "\n" +
                "Consejos para respirar un aire mas limpio\n" +
                "Monitoreo de la calidad del aire a traves de aplicaciones o sitios web que proporcionen informacion en tiempo real sobre los niveles\n" +
                "Evitar actividades al aire libre en momentos criticos evitando actividades las horas pico generalmente en fias calidos y soleados\n" +
                "Usa filtracion en interiores como purificadores de aire y asegurate de tener una buena ventilacion en interiores\n" +
                "OXIDOS DE NITROGENO(NOx)\n" +
                "Causas\n" +
                "Combustion de combustibles fosiles\n" +
                "La principal fuente de óxidos de nitrógeno es la combustión de combustibles fósiles en vehículos, centrales eléctricas y procesos industriales.\n" +
                "\n" +
                "Agricultura\n" +
                "Las emisiones de NOx también pueden provenir de actividades agrícolas, como el uso de fertilizantes.\n" +
                "\n" +
                "Quema de biomasa\n" +
                "La quema de biomasa, como madera y residuos agrícolas, puede liberar óxidos de nitrógeno.\n" +
                "\n" +
                "Efectos\n" +
                "Problemas respiratorios\n" +
                "La exposición a altos niveles de NOx puede irritar las vías respiratorias y empeorar condiciones como el asma.\n" +
                "\n" +
                "Formacion de ozono troposferico\n" +
                "Los óxidos de nitrógeno contribuyen a la formación de ozono troposférico, que puede tener efectos perjudiciales para la salud y el medio ambiente.\n" +
                "\n" +
                "Lluvia acida\n" +
                "Pueden contribuir a la formación de ácido nítrico y ácido nítrico, que están asociados con la lluvia ácida.\n" +
                "\n" +
                "Valores limite\n" +
                "En la Unión Europea, el valor límite para la protección de la salud humana es de 40 microgramos por metro cúbico (µg/m³) como media anual para el dióxido de nitrógeno (NO2).\n" +
                "\n" +
                "Consejos para respirar un aire mas limpio\n" +
                "Uso eficiente de la energia adoptando practicas que mejoren la eficiencia energetica en el hogar\n" +
                "Utiliza combustibles mas limpios y intenta utilizar un vehiculo mas eficiente");

    }
}