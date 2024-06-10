# TIPE 2023

## Introduction

Ce projet de TIPE est une modélisation d'un billard américain. L'objectif de celui-ci est de déterminer le meilleur coup à jouer pour une position donnée.
Pour cela, une première approche a été la simulation d'un grand nombre de lancers. Le programme récupérait alors celui offrant un meilleur score final.

Cette méthode, bien qu'accélérée par l'utilisation d'OpenCL reste néanmoins assez lente et peu satisfaisante.

L'objectif à terme de ce projet est de réunir à l'aide de cette première approche suffisamment de données pour entrainer un réseau de neurones ayant la capacité de substituer cette approche.

## Installation (Java 9)

Installer préalablement Java 9. Télécharger la dernière version compilée du projet, vous êtes prêt à l'utiliser !

## Lancement

Plusieurs choix s'offre à vous pour lancer le programme. Celui-ci est constitué de plusieurs applications différentes.

Pour lancer le programme et tomber sur l'interface d'aide, exécutez simplement

> java -jar &#60;nom du jar&#62;.jar

L'interface d'aide permet d'afficher les différents arguments de lancement à votre disposition.
Vous pouvez les inscrire directement dans la commande de lancement sans passer par l'aide :

> java -jar &#60;nom du jar&#62;.jar game [...]

**/!\ Lors de la fermeture des éventuelles fenêtres, vous allez être redirigé vers la console. Tapez 'quit' pour tuer le programme.**

# Modélisation Physique

## Boules

Les boules sont considérées comme parfaitement sphériques. On suppose qu'elles n'évoluent que dans un espace à 2 dimensions.

### Notations

On numérote chaque boule $B_i$ avec un indice $i \in [0; 15]$. On associe alors à la boule $B_i$ la position $\vec{r_i}$ et la vitesse $\vec{v_i}$. On munit l'espace d'un repère orthonormé $(Oxy)$ de centre $O$ le centre du billard et **tel que chaque boule ait un rayon de 1**.

## Contacts

Les contacts entre une boule et un mur sont sans perte d'énergie, instantanés et respectent les lois de Snell-Descartes de la réflexion.

Les contacts entre une boule $B_i$ et une boule $B_j$ sont également sans perte d'énergie : le transfert d'énergie d'une boule à l'autre est total.

## Frottements

Les frottements avec le sol et l'air sont modélisés par une force de frottement fluide de paramètre massique $\alpha$ (paramétrable)

## Etude des intéractions

### Bilan des forces appliquées (Hors choc)

S'applique sur une boule $B_i$ de masse $m$ et ne choquant pas :

- Une force de frottements fluides : $\vec{f_i} = -\alpha \times m \times \vec{v_i}$

### Equations vérifiées (Hors choc)

Le principe fondamental de la dynamique appliqué à la boule $B_i$ (ne choquant pas, elle peut être assimilée à un point matériel de masse $m$) :

$$
\frac{d^2x}{dt^2} = -\alpha \times \frac{dx}{dt} \quad \text{ et } \quad \frac{d^2y}{dt^2} = -\alpha \times \frac{dy}{dt}
$$

### Etude d'un choc avec un mur

On considère un mur dirigé par un vecteur unitaire $\vec{u}$. Lors d'un contact entre la boule $B_i$ et ce mur, la vitesse $\vec{v_i}$ change instantanément de $\vec{v_i}^-$ à $\vec{v_i}^+$ : $\vec{v_i}^+ = 2 \times \vec{u} \times (\vec{u} \cdot \vec{v_i}^-) - \vec{v_i}^-$

### Etude d'un choc avec une boule

On considère le choc instantané entre deux boules $B_i$ et $B_j$ où $i \neq j$. Ce choc n'a de sens que si $||\vec{r_i} - \vec{r_j}|| \leq 2$.

On pose :

$$
\vec{\delta} = \frac{\vec{r_i} - \vec{r_j}}{2} = \begin{pmatrix} \delta_x \cr \delta_y \cr \end{pmatrix}
$$

Il vient alors :

$$
\begin{cases}
v_{i,x}^+ = v_{i,x}^- + \delta_x \times (\vec{v_j}^- - \vec{v_i}^-) \cdot \vec{\delta} \\\\ v_{i,y}^+ = v_{i,y}^- + \delta_y \times (\vec{v_j}^- - \vec{v_i}^-) \cdot \vec{\delta}
\end{cases}
\quad \text{ et } \quad
\begin{cases}
v_{j,x}^+ = v_{j,x}^- + \delta_x \times (\vec{v_i}^- - \vec{v_j}^-) \cdot \vec{\delta} \\\\ v_{j,y}^+ = v_{j,y}^- + \delta_y \times (\vec{v_i}^- - \vec{v_j}^-) \cdot \vec{\delta}
\end{cases}
$$

# Simulation

On définit une simulation par le calcul des positions de l'ensemble des boules présentes sur le billard. Une simulation commence par l'ajout d'un vecteur vitesse à la boule blanche, et s'arrête lorsque toutes les boules sont à l'arrêt.

## Représentations

Un billard est représenté numériquement par un tableau de dimension $5$ et de taille $16$ (paramétrable) : Pour chaque boule $B_i$, il en faut 2 pour représenter $\vec{r_i}$, 2 pour représenter $\vec{v_i}$ et une pour enregistrer des propriétés propres à la boule (par exemple si la boule est entrée dans un trou). Ce tableau est représenté numériquement par un `float[]` de taille $5 \times 16 = 80$.

On ajoute à cela un tableau de taille $2$ pour stocker des informations propres à la simulation : Un indicateur de mouvement sur le billard, et le score marqué.

## Echantillonnage temporel

L'échantillon de temps est $\Delta t = 1 ms$. Tout évènement d'une durée inférieure à $\Delta t$ est négligé.

## Calcul des positions et vitesses des boules

Le calcul de résolution des équations précédentes est effectué dans le fichier [board.cl](src/main/resources/board.cl). On utilise ainsi l'accélération du processeur graphique pour réaliser très rapidement ces résolutions en parallèle. L'application Java transmet la [représentation du billard](#représentations) à OpenCL, qui renvoie la nouvelle position des boules.

L'algorithme de résolution utilisé est Runge-Kutta d'ordre 4.

## Estimation du meilleur coup

On considère une position de billard donnée. Dans un premier temps, l'estimation du meilleur coup s'effectue en testant un grand nombre de possibilités. On partitionne tous les vecteurs vitesse possibles de la boule blanche en partionnant tous les angles possibles (à $360°$) ainsi que les normes possibles (de $0$ à $300$).

Une étude statistique rapide a permis de sélectionner la taille des partitions pour avoir le plus de bon résultats en proportion du nombre total de simulations. On divise alors le cercle en $500$ angles et la norme en $119$ (de manière homogène).

On obtient alors un total de $500 \times 119 = 59500$ simulations par estimation.

## Résultat de Simulation

À la fin d'une simulation, on appelle **résultat de simulation** (valable pour la suite de ce document) un triplet contenant :

- Le vecteur vitesse de la boule blanche utilisé lors de la simulation : un angle par rapport à l'horizontale, et une norme (tuple de Integers)
- Un score codé sur 2 octets (short) décrivant l'amélioration de la position après ce coup. Si le coup est jugé bon, le score est positif, négatif sinon.

Pour calculer le score, on additionne des scores suivant les actions qui se déroulent :

- La boule blanche est entrée : $-12$.
- Une boule pleine est entrée : $11$.
- Une boule rayée est entrée : $-10$.
- La boule noire est entrée : $\pm1000$. $+1000$ si toutes les boules pleines sont entrées.

*Par exemple, si la boule $3$ et $15$ sont rentrées sur un unique coup de boule blanche, le score sera de $1$.*

# Application JAVA

Voir les instructions d'[installation](#installation-java-9) et de [lancement](lancement).

## Applications intégrées

Pour simplifier les usages et éviter la redondance, un système d'applications internes a été mis en place. Par défaut, le lancement sans argument renvoie sur l'application help.

Ces applications sont :

- help : Permet d'obtenir de l'aide
- game : Lance une partie classique avec possibilité de faire jouer l'ordinateur
- statistic : Calcule différents statistiques sur les simulations.

Pour lancer une application, il faut quitter l'application en cours (fermer la fenêtre java), et taper dans la console :

> app=ID [ARGS]

Par exemple, écrire dans la console `app=game` va lancer l'application Game.

### Help

Permet de lancer les autres applications et d'apporter des informations supplémentaires sur les différents paramètres de lancement des applications.

### Game

Application classique d'un billard jouable. Utiliser la barre d'espace pour lancer un calcul du meilleur coup.

Chercher dans l'application help les différentes descriptions des arguments.

### Statistic

Permet de faire varier le nombre de simulations par position pour trouver un meilleur coup. Renvoie des fichiers `.statistic` pouvant être affichés par le programme python.

## Gestion des données

Cette partie est consacrée à la sauvegarde de position, et au chargement de positions sauvegardées. Cette gestion doit répondre à plusieurs critères :

- L'optimisation de l'espace de stockage pour limiter l'impact des positions sur l'espace de stockage
- La sauvegarde asynchrone d'informations
- La segmentation automatique des informations : Chaque fichier contient un nombre limite d'information, le dépassement de cette limite doit engendrer la création d'un nouveau fichier. Cette procédure permet de limiter la corruption des positions sauvegardées lors d'un crash.
- La lecture "resource-free" des données. L'application ne doit pas placer toute l'information en cache puis la traiter, mais doit charger uniquement ce dont elle a besoin pour limiter l'impact du traitement des données sauvegardées sur la mémoire vive.
- Une abstraction suffisante pour stocker différents types de données avec le même système de stockage.

### Abstraction

Pour répondre à cette nécessité d'abstraction, on ne stocke pas de position en tant que telle, mais on crée une interface `ByteStorable` contenant trois fonctions principales :

- `toBytes()` renvoyant un tableau de byte : Convertit l'objet en tableau de byte
- `fromBytes(byte[])` renvoyant void : Modifie les propriétés de l'objet avec les informations contenues dans le tableau en argument.
- `size()` renvoyant un int : Renvoie la taille du tableau renvoyé par toBytes().

Ainsi qu'une propriété de type `FileMetadata` avec un Getter et un Setter.

Toute implémentation devra impérativement comporter un constructeur sans argument, permettant l'appel de la fonction `fromBytes()`.

Finalement, tout objet implémentant cette interface pourra être stockée avec ce système de stockage.

### Segmentation automatique des informations

L'objectif de ce système de stockage est de permettre de stocker un grand nombre de positions de billard dans un minimum d'espace et de pouvoir naviguer dans ces positions. Chaque position implémente de `ByteStorable` (voir [Abstraction](#abstraction)). On va ensuite regrouper les positions dans un `Storage` qui peut stocker uniquement des `ByteStorable` de même `FileMetadata`, correspondant essantiellement à leur taille sur le disque et aux conditions de simulations (les positions ont une taille fixe, donc peuvent être stockée ainsi).

Chaque Storage représente alors un fichier contenant des positions. Le lien entre `Storage` et le réel stockage dans un fichier se passe au travers de la classe `FileStorage`. Ainsi `Storage` contient les positions en mémoire vive, et `FileStorage` représente un pointeur pour accéder au `Storage`. Manipuler des `FileStorage` ne consomme que très peu d'espace mémoire vive.

Pour regrouper les différents fichiers et effectuer de la segmentation, on utilise la classe `StorageHandler` qui va permettre d'enregistrer un `ByteStorable` en le dispatchant soit dans un fichier non plein (on paramètre au préalable le nombre maximal de `ByteStorable` contenu dans un `Storage`), soit en créant un nouveau fichier (`Storage` et `FileStorage` associé). L'écriture segmentée est donc automatique.

### Lecture "resource-free"

Concernant la lecture, on utilise la classe `StorageReader<ByteStorable>` qui fonctionne comme un `Iterator<ByteStorable>`. Un seul `Storage` est chargé à la fois. On utilise deux indices (l'un est l'indice de la position dans le `Storage`, l'autre est l'indice du `Storage` dans le `StorageHandler`) que l'on fait varier pour obtenir les différents `ByteStorable`.

### Optimisation de l'espace de stockage

Cette partie s'intéresse au protocole de conversion de l'objet `BoardPosition`, `PositionResult` et `FullPosition`. Ces objets implémentent tous `ByteStorable` et se définissent par :

- `BoardPosition` : Représente une position de Billard, ne prend en compte que la position de chaque boule.
- `PositionResult` : Représente le [résultat](#resultat-de-simulation) d'une simulation.
- `FullPosition` : Représente un position et un tableau de résultats.

#### BoardPosition

Un position étant définie par un tableau de $32$ floats, aucune optimisation simple ne peut être appliquée. La taille d'une position est alors fixée à $4 \times 32 = 128$ octets (un float est codé sur $4$ octets).

#### PositionResult

Sur cette partie, une optimisation peut être faite car sur les $3$ valeurs à stocker, quasiment aucune n'ont une valeur suffisante pour justifier de les encoder sur 4 ou 2 octets. Ainsi, suivant la taille des partitions (voir la [description des partitions](#estimation-du-meilleur-coup)), on obtient un majorant pour les deux premiers integers, donc une taille maximale $M_{angle}$ et $M_{norme}$. De même, on majore le score maximal que l'on peut faire en un coup (Rentrer toutes les boules pleines, puis la noire) et on obtient la taille maximal du score $M_{score}$.

Ainsi, on peut coder un PositionResult sur $M_{angle} + M_{norme} + M_{score}$ octets avec le *bitwise*. Cette taille étant nécessairement inférieure ou égale à $4 + 4 + 2 = 10$ octets.

#### FullPosition

Par définition de la classe `FullPosition`, sa représentation binaire n'est que la concaténation d'une `BoardPosition` et d'un tableau de `PositionResult`. Tout est donc mis bout-à-bout, et peut être lu correctement avec la connaissance des tailles sur le disque des différents objets. (Tailles connues grâce à la méthode `size()`)

### Sauvegarde Asynchrone d'informations

Le support de cette fonctionnalité est nécessaire car lors de la génération de positions, on sépare les différentes tâches en différents `Thread`.

La sous-classe `AsyncStorageRegisterer` déclarée à l'intérieur de `StorageHandler` étend de la classe `Thread` et démarre celui-ci au moment de l'appel du constructeur. Il contient une queue (de type `Queue<ByteStorable>`) qui stocke les `ByteStorable` à enregistrer. Le `Thread` parcours en boucle la queue et ajoute chaque élément qu'il rencontre avant de les supprimer de la queue.
