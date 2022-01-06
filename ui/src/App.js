import React from 'react';

import Form from 'react-bootstrap/Form';

import './App.css';

class App extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      matchId: '',
      analysis: {},
      error: false
    };

    this.handleChange = this.handleChange.bind(this)
    this.handleSubmit = this.handleSubmit.bind(this)
  }

  handleChange(event) {
    this.setState({ matchId: event.target.value })
  }

  handleSubmit(event) {
    this.setState({ error: false })
    fetch(`http://localhost:8080/analysis/${this.state.matchId}`)
      .then(response => {
        if (!response.ok) {
          this.setState({ error: true })
        }
        return response.json()
      })
      .then(json => {
        this.setState({ analysis: json })
      })
      .catch(e => {
        this.setState({ error: true })
      })
    event.preventDefault()
  }

  render() {
    const green = {
      color: "green"
    }

    const red = {
      color: "red"
    }

    const analysis = this.state.analysis
    const analysisLoaded = analysis && Object.keys(analysis).length !== 0
    let courierInfo
    if (analysisLoaded) {
      courierInfo = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9].map((id) =>
        <li key={id.toString}>
          {analysis.heroes[id]}'s courier is {analysis.couriers[id] ? <span style={green}>out of</span> : <span style={red}>in</span>} fountain
        </li>
      )
    }

    const paddingTop = {
      paddingTop: 20
    }



    return (
      <div className="App">
        <form className="MatchInput" onSubmit={this.handleSubmit}>
          <Form.Control type="text" placeholder="Enter match id" value={this.state.matchId} onChange={this.handleChange} />
        </form>
        <div style={paddingTop}>
          {analysisLoaded &&
            <ul>{courierInfo}</ul>
          }
        </div>
        {this.state.error &&
          <div> Error occured :( </div>
        }
      </div>
    );
  }
}

export default App;
