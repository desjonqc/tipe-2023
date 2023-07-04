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

Lors de la fermeture des éventuelles fenêtres, vous allez être redirigé vers la console. Tapez 'quit' pour tuer le programme.

# Modélisation Physique

## Boules
Les boules sont considérées comme sphériques. On suppose qu'elles n'évoluent que dans un espace à 2 dimensions.

## Contacts
Les contacts entre une boule et un mur sont sans perte d'énergie et respecte les lois de Snell-Descartes de la réflexion.

Les contacts entre une boule A et une boule B sont également sans perte d'énergie. Le transfert d'énergie d'une boule à l'autre est total, selon le vecteur AB.

## Frottements
Les frottements avec le sol et l'air sont modélisés par une force de frottement fluide de paramètre $\alpha$ (paramétrable)


# Simulation

## Echantillonnage temporel
Le temps est échantillonné en millisecondes. tout évènement d'une durée inférieure à une milliseconde est négligé.

## Principe
L'algorithme utilisé est semblable à l'algorithme d'Euler. A chaque mise à jour de la simulation, est ajouté à la position de chaque boule $\Delta t\*v$, et à la vitesse de la boule $-\alpha*v$

