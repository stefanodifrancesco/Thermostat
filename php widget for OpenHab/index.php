<?php
  $dbhost = 'localhost:3306';
  $dbuser = 'se4as';
  $dbpass = 'se4as';
  $conn = mysqli_connect($dbhost, $dbuser, $dbpass);
   
  if(! $conn ){
    die('Could not connect: ' . mysqli_error());
  }
         
  $data = array(
    'cols' => array(
      array('type' => 'string', 'label' => 'room'),
      array('type' => 'date', 'label' => 'starts'),
      array('type' => 'date', 'label' => 'finish')
    ),
    'rows' => array()
    );

  $result = mysqli_query($conn, "SELECT * FROM se4as.presences WHERE day = DAYNAME(NOW()) AND room = \"" . $_GET['room'] . "\"");

  while($row = mysqli_fetch_array($result)) {

    $startStr = $row['start'];
    $finishStr = $row['end'];

    $start = strtotime($startStr);
    $end = strtotime($finishStr);

    $data['rows'][] = array('c' => array(
        array('v' => 'Predicted presences'),
        array('v' => 'Date(' . Date("0, 0, 0, H, i, s", $start) . ')'),
        array('v' => 'Date(' . Date("0, 0, 0, H, i, s", $end) . ')')
    ));

  }
?>

<html>
  <head>
    <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>


    <script type="text/javascript">

        google.charts.load('current', {'packages':['timeline']});
        google.charts.setOnLoadCallback(drawChart);

        function drawChart() {

          var container = document.getElementById('timeline');
          var chart = new google.visualization.Timeline(container);
          var data = new google.visualization.DataTable(<?php echo json_encode($data, JSON_NUMERIC_CHECK); ?>);

          var options = {
            hAxis: {
              format: 'HH:mm'
            },
            colors: ['red', 'blue', 'green', 'orange'],
            timeline: { showRowLabels: true, colorByRowLabel: true },
            backgroundColor: 'lightgray',
            height: 200
          };

          chart.draw(data, options);
        }
      </script>
  </head>
  
  <body>
    <div id="timeline">
    </div>

  </body>
</html>