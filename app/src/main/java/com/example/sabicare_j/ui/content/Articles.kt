package com.example.sabicare_j.ui.content

/**
 * Curated articles & posts pulled from real, well-known health sites.
 * Each card on the home screen uses a beautiful Unsplash image (URL),
 * a category tag, and a real article link that opens in the browser
 * when the user taps the card.
 */
data class Article(
    val title: String,
    val subtitle: String,
    val source: String,         // WHO / CDC / UNICEF / Mayo Clinic ...
    val category: String,       // Тамақтану / Ұйқы / Даму ...
    val imageUrl: String,       // Internet image (URL)
    val articleUrl: String,     // Opens in browser
    val readMinutes: Int = 5
)

object Articles {

    val featured: List<Article> = listOf(
        Article(
            title = "Емізудің артықшылықтары мен техникасы",
            subtitle = "ДДҰ: алғашқы 6 ай тек ана сүтімен қоректендіру",
            source = "WHO · World Health Organization",
            category = "Тамақтану",
            imageUrl = "https://images.unsplash.com/photo-1607758020330-6a4d2ae9c10c?w=900&q=80",
            articleUrl = "https://www.who.int/news-room/fact-sheets/detail/infant-and-young-child-feeding",
            readMinutes = 6
        ),
        Article(
            title = "Нәрестенің дамуының негізгі кезеңдері",
            subtitle = "CDC: 2 айдан 5 жасқа дейінгі даму белестерінің толық тізімі",
            source = "CDC · Centers for Disease Control",
            category = "Даму",
            imageUrl = "https://images.unsplash.com/photo-1492725764893-90b379c2b6e7?w=900&q=80",
            articleUrl = "https://www.cdc.gov/ncbddd/actearly/milestones/index.html",
            readMinutes = 8
        ),
        Article(
            title = "Қауіпсіз ұйқы: SIDS-тен сақтану жолдары",
            subtitle = "Нәрестені арқасына жатқызу, төсек қандай болуы тиіс",
            source = "AAP · HealthyChildren.org",
            category = "Ұйқы",
            imageUrl = "https://images.unsplash.com/photo-1555252333-9f8e92e65df9?w=900&q=80",
            articleUrl = "https://www.healthychildren.org/English/ages-stages/baby/sleep/Pages/A-Parents-Guide-to-Safe-Sleep.aspx",
            readMinutes = 5
        )
    )

    val all: List<Article> = featured + listOf(
        Article(
            title = "Алғашқы шомылдыру: қадамдық нұсқаулар",
            subtitle = "Кіндігі түспеген нәрестені дұрыс шомылдыру",
            source = "Mayo Clinic",
            category = "Күтім",
            imageUrl = "https://images.unsplash.com/photo-1559757148-5c350d0d3c56?w=900&q=80",
            articleUrl = "https://www.mayoclinic.org/healthy-lifestyle/infant-and-toddler-health/in-depth/healthy-baby/art-20044438",
            readMinutes = 5
        ),
        Article(
            title = "Нәрестенің денесі қызған кезде не істеу керек",
            subtitle = "Қашан дәрігерге қоңырау шалу қажет — белгілер тізімі",
            source = "NHS · UK Health Service",
            category = "Денсаулық",
            imageUrl = "https://images.unsplash.com/photo-1583912267550-d6c2ac3196c0?w=900&q=80",
            articleUrl = "https://www.nhs.uk/conditions/baby/health/fever-in-children/",
            readMinutes = 4
        ),
        Article(
            title = "Толықтаушы тамақ: 6 айдан бастап",
            subtitle = "UNICEF: алғашқы қатты тамақты қалай енгізу керек",
            source = "UNICEF",
            category = "Тамақтану",
            imageUrl = "https://images.unsplash.com/photo-1602131817-fa040f0b3010?w=900&q=80",
            articleUrl = "https://www.unicef.org/parenting/food-nutrition/feeding-your-baby-6-12-months",
            readMinutes = 6
        ),
        Article(
            title = "Д витамині: неге, қашан және қанша беру керек",
            subtitle = "Рахит профилактикасы — туылғаннан бастап",
            source = "AAP",
            category = "Дәрумендер",
            imageUrl = "https://images.unsplash.com/photo-1576092762791-dd9e2220abd1?w=900&q=80",
            articleUrl = "https://www.healthychildren.org/English/healthy-living/nutrition/Pages/Vitamin-D-On-the-Double.aspx",
            readMinutes = 4
        ),
        Article(
            title = "Нәресте гимнастикасы: 1-12 ай",
            subtitle = "Бұлшықетті нығайтуға арналған қарапайым жаттығулар",
            source = "Cleveland Clinic",
            category = "Физикалық даму",
            imageUrl = "https://images.unsplash.com/photo-1502086223501-7ea6ecd79368?w=900&q=80",
            articleUrl = "https://my.clevelandclinic.org/health/articles/14474-baby-development-stages",
            readMinutes = 5
        ),
        Article(
            title = "Колика және жылау: ата-аналарға арналған нұсқаулық",
            subtitle = "Шамамен 4 айға дейінгі балалардағы колика",
            source = "Mayo Clinic",
            category = "Денсаулық",
            imageUrl = "https://images.unsplash.com/photo-1623091410901-00e2d268901f?w=900&q=80",
            articleUrl = "https://www.mayoclinic.org/diseases-conditions/colic/symptoms-causes/syc-20371074",
            readMinutes = 5
        ),
        Article(
            title = "Тіс шығу кезеңі: белгілер мен көмек",
            subtitle = "6 айдан бастап — тіс шығудың әр кезеңі",
            source = "WebMD",
            category = "Даму",
            imageUrl = "https://images.unsplash.com/photo-1546015720-b8b30df5aa27?w=900&q=80",
            articleUrl = "https://www.webmd.com/parenting/baby/baby-teething-symptoms",
            readMinutes = 4
        ),
        Article(
            title = "Емізетін ана үшін тағам туралы кеңестер",
            subtitle = "Не жеуге болады, неден аулақ болу керек",
            source = "Mayo Clinic",
            category = "Тамақтану",
            imageUrl = "https://images.unsplash.com/photo-1517423568366-8b83523034fd?w=900&q=80",
            articleUrl = "https://www.mayoclinic.org/healthy-lifestyle/infant-and-toddler-health/in-depth/breastfeeding-nutrition/art-20046912",
            readMinutes = 7
        ),
        Article(
            title = "Емшекті тастау: бөтелке мен ара-арасында берудің әдептері",
            subtitle = "Жеткілікті мөлшерде болғанша және одан кейін",
            source = "La Leche League",
            category = "Тамақтану",
            imageUrl = "https://images.unsplash.com/photo-1503454537195-1dcabb73ffb9?w=900&q=80",
            articleUrl = "https://llli.org/breastfeeding-info/weaning/",
            readMinutes = 6
        ),
        Article(
            title = "Эмоциялық даму: байланыс пен сезім",
            subtitle = "Қауіпсіз байланыс негізі — алғашқы жыл маңызды",
            source = "Zero to Three",
            category = "Психология",
            imageUrl = "https://images.unsplash.com/photo-1530021232320-687d8e3dba54?w=900&q=80",
            articleUrl = "https://www.zerotothree.org/resource/healthy-from-the-start-how-feeding-nurtures-your-young-childs-body-heart-and-mind/",
            readMinutes = 5
        ),
        Article(
            title = "Екпелер кестесі: міндетті прививкалар",
            subtitle = "Туылғаннан 24 айға дейін — ҚР Денсаулық министрлігі",
            source = "WHO · Vaccination Schedule",
            category = "Денсаулық",
            imageUrl = "https://images.unsplash.com/photo-1583912267550-d6c2ac3196c0?w=900&q=80",
            articleUrl = "https://www.who.int/teams/immunization-vaccines-and-biologicals/policies/who-recommendations-for-routine-immunization---summary-tables",
            readMinutes = 6
        ),
        Article(
            title = "Тілдің дамуы: бірінші сөзден сөйлемге дейін",
            subtitle = "Не нәресте айтуы керек 6, 9, 12, 18 айда",
            source = "ASHA",
            category = "Даму",
            imageUrl = "https://images.unsplash.com/photo-1455620611406-966ca6889d2c?w=900&q=80",
            articleUrl = "https://www.asha.org/public/speech/development/01/",
            readMinutes = 5
        ),
        Article(
            title = "Ойындар арқылы даму: 0-12 ай",
            subtitle = "Сенсорлық, моторлы дамуды қолдайтын ойындар",
            source = "Pathways.org",
            category = "Ойын",
            imageUrl = "https://images.unsplash.com/photo-1573676574053-08e8c0e2f49b?w=900&q=80",
            articleUrl = "https://pathways.org/all-ages/milestones/",
            readMinutes = 4
        )
    )

    /** Витаминдер мен препараттар — әр карточка ашық дереккөзге сілтейді */
    val medications: List<Medication> = listOf(
        Medication(
            name = "Д витамині",
            description = "Сүйек жүйесін нығайтады, рахитті алдын алады",
            ageRange = "0+ ай",
            imageUrl = "https://images.unsplash.com/photo-1559056961-84efbc8d6f6c?w=600&q=80",
            sourceUrl = "https://www.healthychildren.org/English/healthy-living/nutrition/Pages/Vitamin-D-On-the-Double.aspx"
        ),
        Medication(
            name = "Темір препараттары",
            description = "Анемияның алдын алу үшін қажет — нәрестелерге",
            ageRange = "4+ ай",
            imageUrl = "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=600&q=80",
            sourceUrl = "https://www.cdc.gov/breastfeeding/breastfeeding-special-circumstances/diet-and-micronutrients/iron.html"
        ),
        Medication(
            name = "Омега-3 (DHA)",
            description = "Мидың дамуы мен иммунитет үшін",
            ageRange = "6+ ай",
            imageUrl = "https://images.unsplash.com/photo-1607619056574-7a8f6c4d27e1?w=600&q=80",
            sourceUrl = "https://www.healthychildren.org/English/healthy-living/nutrition/Pages/Omega-3-Fats-Good-for-the-Heart-and-the-Mind.aspx"
        ),
        Medication(
            name = "Пробиотиктер",
            description = "Ішек микрофлорасын қалпына келтіреді",
            ageRange = "0+ ай (дәрігер кеңесімен)",
            imageUrl = "https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=600&q=80",
            sourceUrl = "https://www.healthychildren.org/English/healthy-living/nutrition/Pages/probiotics-and-prebiotics-in-children.aspx"
        ),
        Medication(
            name = "Поливитаминдер",
            description = "Бір жасқа дейін кешенді витамин қажет емес",
            ageRange = "1+ жас",
            imageUrl = "https://images.unsplash.com/photo-1550572017-edd951b55104?w=600&q=80",
            sourceUrl = "https://www.healthychildren.org/English/healthy-living/nutrition/Pages/Vitamins-for-Kids.aspx"
        )
    )
}

data class Medication(
    val name: String,
    val description: String,
    val ageRange: String,
    val imageUrl: String,
    val sourceUrl: String
)
