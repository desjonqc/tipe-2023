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

> java -jar &#60;nom du jar&#62;.jar app=game [...]

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

S'applique sur une boule $B_i$ ne choquant pas :

- Une force de frottements fluides : $\vec{f_i} = -\alpha \times m \times \vec{v_i}$

### Equations vérifiées (Hors choc)

Le principe fondamental de la dynamique appliqué à la boule $B_i$ (ne choquant pas, elle peut être assimilée à un point) :

$$
\frac{d^2x}{dt^2} = -\alpha \times \frac{dx}{dt} \quad \text{ et } \quad \frac{d^2y}{dt^2} = -\alpha \times \frac{dy}{dt}
$$

### Etude d'un choc avec un mur

On considère un mur dirigé par un vecteur unitaire $\vec{u}$. Lors d'un contact entre la boule $B_i$ et ce mur, la vitesse $\vec{v_i}$ change instantanément de $\vec{v_i}^-$ à $\vec{v_i}^+$ : $\vec{v_i}^+ = 2 \times \vec{u} \times (\vec{u} \cdot \vec{v_i}^-) - \vec{v_i}^-$

### Etude d'un choc avec une boule

On considère le choc instantané entre deux boules $B_i$ et $B_j$ où $i \neq j$. Ce choc n'a de sens que si $||\vec{r_i} - \vec{r_j}|| \leq 2$.

On pose $\vec{\delta} = \frac{\vec{r_i} - \vec{r_j}}{2} = \begin{pmatrix}\delta_x\cr\delta_y\cr\end{pmatrix}$

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

Le calcul de résolution des équations précédentes est effectué dans le fichier [board.cl](src/main/resources/board.cl). On utilise ainsi l'accélération du processeur graphique pour réaliser très rapidement ces résolutions en parallèle. L'application Java transmet la [représentation du billard](#représentations)

L'algorithme utilisé est sembable à celui d'Euler explicite.


# Application JAVA

Voir les instructions d'[installation](#installation-java-9) et de [lancement](lancement).

## Applications intégrées

Pour simplifier les usages et éviter la redondance, un système d'applications internes a été mis en place. Par défaut, le lancement sans argument renvoie sur l'application help.

Ces applications sont :

- help : Permet d'obtenir de l'aide
- game : Lance une partie classique avec possibilité de faire jouer l'ordinateur
- statistic : Calcule différents statistiques sur les simulations.

Pour lancer une application, il faut soit quitter l'application en cours (fermer la fenêtre), soit taper dans la console :

> app=ID [ARGS]

Par exemple, écrire dans la console `app=game` va lancer l'application Game.

### Help

Permet de lancer les autres applications et d'apporter des informations supplémentaires sur les différents paramètres de lancement.

### Game

Application classique d'un billard jouable. Utiliser la barre d'espace pour lancer un calcul du meilleur coup.

Chercher dans l'application help les différentes descriptions des arguments.

### Statistic

Permet de faire varier le nombre de simulations par position pour trouver un meilleur coup. Renvoie des fichiers `.statistic` pouvant être affichés par le programme python.

## Gestion des données

Todo
