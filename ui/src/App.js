import './App.css';
import React from 'react';

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
    fetch(`http://localhost:8080/analysis/${this.state.matchId}`)
      .then(response => {
        if (!response.ok) {
          this.setState({ error: true })
        }
        response.json()
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
    const analysis = this.state.analysis
    const analysisLoaded = analysis && Object.keys(analysis).length !== 0
    let courierInfo
    if (analysisLoaded) {
      courierInfo = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9].map((id) =>
        <li key={id.toString}>
          {analysis.heroes[id]} courier is {analysis.couriers[id] ? "out of" : "in"} fountain
        </li>
      )
    }

    return (
      <div className="App">
        <form onSubmit={this.handleSubmit}>
          <input placeholder="Enter match id" type="text" value={this.state.matchId} onChange={this.handleChange} />
        </form>
        {analysisLoaded &&
          <ul>{courierInfo}</ul>
        }
        {this.state.error &&
          <div> Error occured :( </div>
        }
      </div>
    );
  }
}

export default App;
