package me.trolloks.dndclient.activities

import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.*
import me.trolloks.dndclient.R
import me.trolloks.dndclient.gateways.dnd5eapi.RaceService
import me.trolloks.dndclient.models.AbilityScore
import me.trolloks.dndclient.models.Skill
import me.trolloks.dndclient.models.unused.Race
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

class MainActivity : AppCompatActivity(), CoroutineScope, AdapterView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener {


    var raceService: RaceService? = null
    var race: Race? = null

    var selectedSkill:Skill? = null
    var proficiency:Int? = null
    var defaultAbility:String = "15"
    var defaultProf:String = "2"

    var skills = arrayListOf<Skill>(
        Skill("Strength", "str", null),
        Skill("Dexterity", "dex", null),
        Skill("Intelligence", "int", null),
        Skill("Wisdom", "wis", null),
        Skill("Constitution", "con", null),
        Skill("Charisma", "cha", null),
        Skill("Athletics", "athletics", "str"),
        Skill("Acrobatics", "acrobatics", "dex"),
        Skill("Sleight of Hand", "sleightofhand", "dex"),
        Skill("Stealth", "stealth", "dex"),
        Skill("Arcana", "arcana", "int"),
        Skill("History", "history", "int"),
        Skill("Investigation", "investigation", "int"),
        Skill("Nature", "nature", "int"),
        Skill("Religion", "religion", "int"),
        Skill("Animal Handling", "animalhandling", "wis"),
        Skill("Insight", "insight", "wis"),
        Skill("Medicine", "medicine", "wis"),
        Skill("Perception", "perception", "wis"),
        Skill("Survival", "survival", "wis"),
        Skill("Persuasion", "persuasion", "cha"),
        Skill("Performance", "performance", "cha"),
        Skill("Intimidation", "intimidation", "cha"),
        Skill("Deception", "deception", "cha")
    )

    var abilities = arrayListOf<AbilityScore>(
        AbilityScore("Strength", "str"),
        AbilityScore("Dexterity", "dex"),
        AbilityScore("Intelligence", "int"),
        AbilityScore("Wisdom", "wis"),
        AbilityScore("Constitution", "con"),
        AbilityScore("Charisma", "cha")
    )

    var BASE_ROLL = 20;

    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        // Setup api
        /*val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("http://dnd5eapi.co/api/")
            .build()

        raceService = retrofit.create(RaceService::class.java)

        launch {
            race = async(Dispatchers.IO) { getRaces() }.await()
            mainText.text = race!!.name
        }*/


        fab.setOnClickListener { view ->
            launch {
                rollDice()
            }

        }
        var values = skills.flatMap { listOf(it.name) }
        val aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, values)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerSkill.adapter = aa;
        spinnerSkill.onItemSelectedListener = this

        checkBox.setOnCheckedChangeListener(this)

        button.setOnClickListener { view ->
            val prefMan = PreferenceManager.getDefaultSharedPreferences(this)
            prefMan.edit().clear().commit()

            var ability:AbilityScore? = null
            if (selectedSkill!!.parentCode == null)
                ability = abilities.filter { it.code.equals(selectedSkill!!.code) }.first();
            else
                ability = abilities.filter { it.code.equals(selectedSkill!!.parentCode) }.first();

            ability.score = null
            proficiency = null
            checkAbilities(ability)
        }
    }

    override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
        if (selectedSkill != null){
            selectedSkill!!.hasSkill = p1
        }
    }

    fun initAbilities(){
        val prefMan = PreferenceManager.getDefaultSharedPreferences(this)
        if (selectedSkill != null){
            var ability:AbilityScore? = null
            if (selectedSkill!!.parentCode == null)
                ability = abilities.filter { it.code.equals(selectedSkill!!.code) }.first();
            else
                ability = abilities.filter { it.code.equals(selectedSkill!!.parentCode) }.first();

            if (!abilityText.text.isEmpty()){
                ability.score = abilityText.text.toString().toInt()
                prefMan.edit().putString(ability.code, abilityText.text.toString()).apply()
            }

            if (!profText.text.isEmpty()) {
                proficiency = profText.text.toString().toInt()
                prefMan.edit().putString("prof", profText.text.toString()).apply()
            }
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {}

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
        var ability:AbilityScore? = null

        selectedSkill = skills[position]
        if (selectedSkill!!.parentCode != null){
            checkBox.visibility = View.VISIBLE
            checkBox.isChecked = selectedSkill!!.hasSkill
            ability = abilities.filter { it.code.equals(selectedSkill!!.parentCode) }.first();
        }
        else {
            ability = abilities.filter { it.code.equals(selectedSkill!!.code) }.first();
            checkBox.visibility = View.GONE
        }

        profText.setText("")
        abilityText.setText("")
        checkAbilities(ability)
    }

    fun checkAbilities(ability:AbilityScore){
        val prefMan = PreferenceManager.getDefaultSharedPreferences(this)
        if (prefMan.contains(ability.code)){
            ability.score = prefMan.getString(ability.code, defaultAbility)!!.toInt();
            abilityContainer.visibility = View.GONE
        } else {
            abilityContainer.visibility = View.VISIBLE
        }

        if (prefMan.contains("prof")){
            proficiency = prefMan.getString("prof", defaultProf)!!.toInt();
            profContainer.visibility = View.GONE
        } else {
            profContainer.visibility = View.VISIBLE
        }

        if (profContainer.visibility == View.GONE && abilityContainer.visibility == View.GONE)
            textView2.visibility = View.GONE
        else
            textView2.visibility = View.VISIBLE

        abilityHeader.setText(ability.name)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private suspend fun rollDice(){
        initAbilities()

        var ability:AbilityScore? = null
        if (selectedSkill!!.parentCode == null)
            ability = abilities.filter { it.code.equals(selectedSkill!!.code) }.first();
        else
            ability = abilities.filter { it.code.equals(selectedSkill!!.parentCode) }.first();

        val thisProficiency = (if (selectedSkill!!.hasSkill) proficiency!! else 0)
        val mod = ability.getModifier() + thisProficiency
        if (mod >= 0)
            rollMod.text = "+" + Math.abs(mod)
        else
            rollMod.text = "-" + Math.abs(mod)

        if (ability.score == null) {
            abilityText.error = "Need Value"

            Snackbar.make(fab, "No values can be empty", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            return;
        }

        if (proficiency == null) {
            profText.error = "Need Value"

            Snackbar.make(fab, "No values can be empty", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            return;
        }


        abilityContainer.visibility = View.GONE
        profContainer.visibility = View.GONE
        textView2.visibility = View.GONE

        var roll = 0
        for (i in 0..50){
            roll = Random.nextInt(1, BASE_ROLL + 1)
            rollText.text = roll.toString();

            if (roll == 20)
                rollText.setTextColor(ContextCompat.getColor(this, R.color.colorGood))
            else if (roll == 1)
                rollText.setTextColor(ContextCompat.getColor(this, R.color.colorBad))
            else
                rollText.setTextColor(ContextCompat.getColor(this, R.color.primary_text_light))

            delay(20)
        }

    }

    /*suspend fun getRaces(): Race {
        var actualRace: Race? = null;
        val call = raceService?.list()
        val response = call?.execute()
        val successful = response?.isSuccessful
        if (successful!!){
            val races = response.body()!!
            for(race in races.results){
                println(race.name)
                actualRace = race;
            }
        }

        return actualRace!!
    }*/

    /*override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when(item.itemId) {
            R.id.action_setup -> true
            else -> super.onOptionsItemSelected(item)
        }
    }*/
}
